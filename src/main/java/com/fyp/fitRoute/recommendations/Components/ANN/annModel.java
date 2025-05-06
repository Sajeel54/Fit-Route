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
public class annModel{
    private MultiLayerNetwork model;
    private dataPreprocessor dataConverter;
    @Autowired
    private MongoTemplate mongoCon;
    @Autowired
    private cloudinaryService cloudinaryService;


    public annModel() {
        buildModel();

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

    public void trainModel() {
        // Get dataset
        DataSet fullData = dataConverter.dataConverter();
        if (fullData == null || fullData.numExamples() < 2) {
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
        int numEpochs = 200;
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

    public double predict(posts post){
        INDArray input = dataConverter.convertPost(post);

        INDArray output = model.output(input);
        return output.getDouble(0); // Probabi
    }

}