package com.fyp.fitRoute.recommendations.Components.ANN;

import com.fyp.fitRoute.posts.Entity.posts;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class dataPreprocessor {
    private List<posts> data;
    private List<posts> likedPosts;
    private List<String> likedIds;
    @Autowired
    private w2vService w2vService;

    public void addPosts(List<posts> data, List<posts> likedPosts){
        this.data = data;
        this.likedPosts = likedPosts;
        likedIds = likedPosts.stream()
                .map(posts::getId)
                .toList();
        w2vService.addLikedPosts(likedPosts);
        w2vService.trainWord2Vec();
    }

    public DataSet dataConverter(){
        List<double[]> featuresList = new ArrayList<>();
        List<double[]> labelsList = new ArrayList<>();

        data.forEach((post)->{
            double category = ((Objects.equals(post.getCategory(), "running"))? 0.0 : 1.0);
            int accountLiking = (likedPosts.stream().filter(p-> Objects.equals(p.getAccountId(), post.getAccountId())).toList()).size();
            double liked = ((likedIds.contains(post.getId()))? 1.0 : 0.0 );
            double descSimilarity = w2vService.getDescSimilarity(post.getDescription());
            double tagSimilarity = w2vService.getTagsSimilarity(post.getTags());
            featuresList.add(new double[]{category, accountLiking, descSimilarity, tagSimilarity});
            labelsList.add(new double[]{ liked });
        });
        INDArray features = Nd4j.create(featuresList.toArray(new double[0][0]));
        INDArray labels = Nd4j.create(labelsList.toArray(new double[0][0]));

        return new DataSet(features, labels);
    }

    public INDArray convertPost(posts post){
        double category = Objects.equals(post.getCategory(), "running") ? 0.0 : 1.0;
        int accountLiking = (likedPosts.stream().filter(p-> Objects.equals(p.getAccountId(), post.getAccountId())).toList()).size();
        double descSimilarity = w2vService.getDescSimilarity(post.getDescription());
        double tagSimilarity = w2vService.getTagsSimilarity(post.getTags());

        return Nd4j.create(new double[]{category, accountLiking, descSimilarity, tagSimilarity}, new int[]{1, 4});
    }
}
