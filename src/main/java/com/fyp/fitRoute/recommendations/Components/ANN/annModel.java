package com.fyp.fitRoute.recommendations.Components.ANN;

import com.fyp.fitRoute.inventory.Services.cloudinaryService;
import com.fyp.fitRoute.inventory.Services.redisService;
import com.fyp.fitRoute.posts.Entity.likes;
import com.fyp.fitRoute.posts.Entity.posts;
import com.fyp.fitRoute.posts.Entity.route;
import com.fyp.fitRoute.posts.Utilities.postResponse;
import com.fyp.fitRoute.recommendations.Utilities.Filter;
import com.fyp.fitRoute.security.Entity.User;
import com.fyp.fitRoute.security.Repositories.userCredentialsRepo;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;


@Component
@Slf4j
public class annModel implements Filter {
    private MultiLayerNetwork model;
    @Autowired
    private dataPreprocessor dataConverter;
    @Autowired
    private MongoTemplate mongoCon;
    @Autowired
    private cloudinaryService cloudinaryService;
    @Autowired
    private userCredentialsRepo userRepo;
    @Autowired
    private redisService redisService;

    private Date accessTimeStamp;

    private List<String> postIds;


    public annModel() {
        buildModel();

    }

    @Override
    public List<String> getSentPosts() {
        return postIds;
    }

    @Override
    public void addPostIds(List<String> postIds) {
        this.postIds = postIds;
    }

    @Override
    public void setTimeStamp(Date accessTimeStamp) {
        this.accessTimeStamp = accessTimeStamp;
    }

    public void readModel(String url) throws Exception {
        File modelFile = cloudinaryService.downloadFile(url, "model.zip");

        // Step 2: Load the model using DL4J
        model = MultiLayerNetwork.load(modelFile, true);

        // Step 3: Clean up the temporary file
        modelFile.delete();
    }

    @Override
    public List<postResponse> getPosts(String myId) throws Exception {
        List<postResponse> responseList = new ArrayList<>();
        List<posts> posts = getNewPosts(myId);
        if (posts.isEmpty()) return responseList;
        Optional<User> myData = userRepo.findById(myId);
        if (myData.isEmpty()) throw new RuntimeException("User not found");
        readModel(myData.get().getModelUrl());
        posts.forEach(post -> {
            if (!(postIds.contains(post.getId()))){
                double pred = predict(post);
                if (pred >= 0.5){
                    postResponse response;
                    User user = mongoCon.findOne(new Query(Criteria.where("id").is(post.getAccountId())), User.class);
                    if (user != null) {
                        response = new postResponse(
                                post.getId(), post.getTitle(), post.getLikes(), post.getComments(),
                                user.getUsername(), user.getImage(), post.getDescription(),
                                post.getTags(), post.getImages(), post.getCategory(),
                                post.getCreatedAt(), post.getUpdatedAt(), false,
                                mongoCon.findOne(
                                        new Query(Criteria.where("id").is(post.getRouteId())), route.class
                                )
                        );
                        responseList.add(response);
                        postIds.add(response.getId());
                    }
                }
            }
        });
        return responseList;
    }

    public void addConverter(dataPreprocessor dataConverter) {
        this.dataConverter = dataConverter;
    }

    public void buildModel() {
        int inputSize = 4; // category, accountLiking, descSimilarity, tagSimilarity
        int outputSize = 1; // liked (0.0 or 1.0)
        double learningRate = 0.001;

        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(42)
                .optimizationAlgo(org.deeplearning4j.nn.api.OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(learningRate))
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(inputSize)
                        .nOut(16)
                        .activation(Activation.RELU)
                        .weightInit(org.deeplearning4j.nn.weights.WeightInit.XAVIER)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(16)
                        .nOut(8)
                        .activation(Activation.RELU)
                        .weightInit(org.deeplearning4j.nn.weights.WeightInit.XAVIER)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.XENT)
                        .nIn(8)
                        .nOut(outputSize)
                        .activation(Activation.SIGMOID)
                        .weightInit(org.deeplearning4j.nn.weights.WeightInit.XAVIER)
                        .build())
                .build();

        model = new MultiLayerNetwork(config);
        model.init();
        model.setListeners(new ScoreIterationListener(100)); // Print score every 100 iterations
    }

    public void trainModel(String myId) {
        List<postResponse> responseList = new ArrayList<>();
        List<posts> postsList = getAllPosts();
        List<posts> likedPosts = getLikedPosts(myId);
        if (postsList.isEmpty() || likedPosts.isEmpty()) return;
        dataConverter.addPosts(postsList, likedPosts);
        // Get dataset
        DataSet fullData = dataConverter.dataConverter();
        if (fullData == null || fullData.numExamples() == 0) {
            System.out.println("No data available for training.");
            return;
        }

        // Shuffle and split (80% train, 20% test)
        fullData.shuffle(42);
        SplitTestAndTrain split = fullData.splitTestAndTrain(0.8);
        DataSet trainData = split.getTrain();
        DataSet testData = split.getTest();

        // Create iterators
        List<DataSet> trainList = new ArrayList<>();
        trainData.asList().forEach(trainList::add);
        DataSetIterator trainIterator = new ListDataSetIterator<>(trainList, 32);

        // Train
        int numEpochs = 100;
        for (int i = 0; i < numEpochs; i++) {
            model.fit(trainIterator);
            trainIterator.reset();
        }

        // Evaluate
        org.deeplearning4j.eval.Evaluation eval = new org.deeplearning4j.eval.Evaluation(1);
        INDArray testFeatures = testData.getFeatures();
        INDArray testLabels = testData.getLabels();
        INDArray predictions = model.output(testFeatures);
        eval.eval(testLabels, predictions);
    }

    @Transactional
    public void saveModel(String myId) {
        File modelFile = new File("model.zip");
        try {
            User user = userRepo.findById(myId).orElseThrow(() -> new RuntimeException("User not found"));
            model.save(modelFile);
            String url = cloudinaryService.uploadModel(modelFile, myId);
            user.setModelUrl(url);
            userRepo.save(user);
        } catch (IOException e) {
            log.error("Error saving model: {}", e.getMessage());
        }
    }

    public double predict(posts post){
        INDArray input = dataConverter.convertPost(post);

        INDArray output = model.output(input);
        return output.getDouble(0); // Probabi
    }

    @Scheduled(fixedRate = 7 * 24 * 60 * 60 * 1000) // 7 days
    public void retrainModel() {
        List<User> users = userRepo.findAll();
        log.info("Retraining model...");
        for (User user : users) {
            try {
                log.info("retraining model for user: id={} / name={} ", user.getId(), user.getUsername());

                accessTimeStamp = Objects.requireNonNullElseGet(
                        redisService.get("Recommendations Access " + user.getId(), Date.class),
                        () -> Date.from(Instant.now().minusSeconds(48L * 60L * 60L))
                );
                trainModel(user.getId());
                saveModel(user.getId());
                accessTimeStamp = null;
            } catch (Exception e) {
                log.error("Error retraining model for user: id={} / name={} : {}", user.getId(), user.getUsername(), e.getMessage());
            }
        }
        log.info("Model retrained.");

    }

    public void clearModel() {
        model.clear();
    }

    public List<posts> getAllPosts(){
//        Date date = new Date(accessTimeStamp.getTime()-(48*60*48*100));
//        return mongoCon.findAll(posts.class)
//                .stream()
//                .filter(post -> (post.getCreatedAt().after(date) && post.getCreatedAt().before(accessTimeStamp)))
//                .toList();
        return mongoCon.findAll(posts.class)
                .stream()
                .filter(post -> post.getCreatedAt().before(accessTimeStamp))
                .toList();
    }

    public List<posts> getNewPosts(String myId){
        Query query = new Query(Criteria.where("createdAt").gt(accessTimeStamp));
        List<String> tempIds  = mongoCon.find(query, posts.class).stream()
                .filter(post -> !post.getAccountId().equals(myId))
                .map(posts::getId)
                .toList();

        tempIds = filterLikedPosts(tempIds, myId);
        query = new Query(Criteria.where("id").in(tempIds));
        return mongoCon.find(query, posts.class);
    }

    private List<String> filterLikedPosts(List<String> ids, String myId) {
        // Step 1: Find postIds liked by myId
        Query query = new Query(Criteria.where("postId").in(ids)
                .and("accountId").is(myId));

        // Get the postIds liked by myId
        List<String> likedByMe = mongoCon.find(query, likes.class).stream()
                .map(likes::getReferenceId)
                .distinct() // Ensure no duplicates
                .toList();

        // Step 2: Return ids not present in likedByMe
        return ids.stream()
                .filter(postId -> !likedByMe.contains(postId))
                .toList();
    }

    private List<posts> getLikedPosts(String myId){
        Query query = new Query(Criteria.where("accountId").is(myId));
        List<likes> temp = mongoCon.find(query, likes.class);

        List<String> tempIds = temp.stream()
                .map(likes::getReferenceId)
                .toList();

        query = new Query(Criteria.where("id").in(tempIds));

        return mongoCon.find(query, posts.class);
    }

}