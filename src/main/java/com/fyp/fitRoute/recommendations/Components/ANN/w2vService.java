package com.fyp.fitRoute.recommendations.Components.ANN;

import com.fyp.fitRoute.posts.Entity.posts;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class w2vService {
    private Word2Vec word2Vec;
    private final int vectorSize = 100; // Adjustable
    private List<posts> likedPosts;
    private List<String> sentences;


    public void addLikedPosts(List<posts> likedPosts) {
        this.likedPosts = likedPosts;
        trainWord2Vec(); // Retrain when new liked posts are added
    }

    public void trainWord2Vec() {
        if (likedPosts == null || likedPosts.isEmpty()) {
            sentences = new ArrayList<>();
            word2Vec = null; // No model if no data
            return;
        }

        // Collect text data
        sentences = likedPosts.stream()
                .flatMap(post -> {
                    List<String> texts = new ArrayList<>();
                    texts.add(post.getDescription().toLowerCase());
                    texts.addAll(post.getTags().stream().map(String::toLowerCase).toList());
                    return texts.stream();
                })
                .collect(Collectors.toList());

        // Train
        CollectionSentenceIterator iterator = new CollectionSentenceIterator(sentences);
        DefaultTokenizerFactory tokenizer = new DefaultTokenizerFactory();

        word2Vec = new Word2Vec.Builder()
                .minWordFrequency(1)
                .iterations(5)
                .layerSize(vectorSize)
                .seed(42)
                .windowSize(5)
                .iterate(iterator)
                .tokenizerFactory(tokenizer)
                .build();

        word2Vec.fit();
    }

    // Helper: Convert text (sentence or word) to vector
    private INDArray getVector(String text) {
        if (word2Vec == null) return Nd4j.zeros(vectorSize);
        String[] tokens = text.toLowerCase().split("\\s+");
        List<INDArray> vectors = new ArrayList<>();
        for (String token : tokens) {
            if (word2Vec.hasWord(token)) {
                vectors.add(word2Vec.getWordVectorMatrix(token));
            }
        }
        return vectors.isEmpty() ? Nd4j.zeros(vectorSize) : Nd4j.mean(Nd4j.vstack(vectors), 0);
    }

    // Helper: Convert tags to vector
    private INDArray getTagsVector(List<String> tags) {
        if (word2Vec == null) return Nd4j.zeros(vectorSize);
        List<INDArray> vectors = new ArrayList<>();
        for (String tag : tags) {
            if (word2Vec.hasWord(tag.toLowerCase())) {
                vectors.add(word2Vec.getWordVectorMatrix(tag.toLowerCase()));
            }
        }
        return vectors.isEmpty() ? Nd4j.zeros(vectorSize) : Nd4j.mean(Nd4j.vstack(vectors), 0);
    }

    public double getDescSimilarity(String desc) {
        if (sentences == null || sentences.isEmpty() || word2Vec == null) {
            return 0.0;
        }

        INDArray descVector = getVector(desc);
        double totalSimilarity = 0.0;

        for (String s : sentences) {
            INDArray sentenceVector = getVector(s);
            double similarity = computeCosineSimilarity(descVector, sentenceVector);
            totalSimilarity += Double.isNaN(similarity) ? 0.0 : similarity;
        }

        return totalSimilarity;
    }

    public double getTagsSimilarity(List<String> tags) {
        if (sentences == null || sentences.isEmpty() || word2Vec == null) {
            return 0.0;
        }

        INDArray tagsVector = getTagsVector(tags);
        double totalSimilarity = 0.0;

        for (String s : sentences) {
            INDArray sentenceVector = getVector(s);
            double similarity = computeCosineSimilarity(tagsVector,sentenceVector);
            totalSimilarity += Double.isNaN(similarity) ? 0.0 : similarity;
        }

        return totalSimilarity;
    }

    // Helper: Compute cosine similarity without dot()
    private double computeCosineSimilarity(INDArray vector1, INDArray vector2) {
        // Manual dot product: element-wise multiplication and sum
        INDArray product = vector1.mul(vector2); // Element-wise multiplication
        double dotProduct = product.sumNumber().doubleValue(); // Sum all elements

        // Norms
        double norm1 = vector1.norm2Number().doubleValue(); // L2 norm
        double norm2 = vector2.norm2Number().doubleValue();

        // Cosine similarity
        double denominator = norm1 * norm2 + 1e-6; // Avoid division by zero
        return dotProduct / denominator;
    }

}