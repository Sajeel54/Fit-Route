package com.fyp.fitRoute.recommendations.Components;

import com.fyp.fitRoute.posts.Entity.likes;
import com.fyp.fitRoute.posts.Entity.posts;
import com.fyp.fitRoute.posts.Entity.route;
import com.fyp.fitRoute.posts.Utilities.postResponse;
import com.fyp.fitRoute.recommendations.Components.ANN.annModel;
import com.fyp.fitRoute.recommendations.Components.ANN.dataPreprocessor;
import com.fyp.fitRoute.recommendations.Utilities.Filter;
import com.fyp.fitRoute.security.Entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class annFilter implements Filter {
    @Autowired
    private MongoTemplate mongoCon;

    private Date accessTimeStamp;

    private List<String> postIds;

    @Autowired
    private annModel model;

    @Autowired
    private dataPreprocessor dataProcessor;


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

    @Override
    public List<postResponse> getPosts(String myId) {
        List<postResponse> responseList = new ArrayList<>();
        List<posts> posts = getNewPosts(myId);
        if (posts.isEmpty()) return responseList;
        List<posts> postsList = getAllPosts();
        List<posts> likedPosts = getLikedPosts(myId);

        dataProcessor.addPosts(postsList, likedPosts);
        model.addConverter(dataProcessor);
        posts.forEach(post -> {
            if (!(postIds.contains(post.getId()))){
               double pred = model.predict(post);
               log.info(pred + " " + post.getId());
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
        model.modelClear();
        return responseList;
    }

    public List<posts> getAllPosts(){
        Date date = new Date(accessTimeStamp.getTime()-(48*60*48*100));
        return mongoCon.findAll(posts.class)
                .stream()
                .filter(post -> (post.getCreatedAt().after(date) && post.getCreatedAt().before(accessTimeStamp)))
                .toList();
//        return mongoCon.findAll(posts.class)
//                .stream()
//                .filter(post -> post.getCreatedAt().before(accessTimeStamp))
//                .toList();
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
        List<likes> temp = mongoCon.find(query, likes.class).stream()
                .filter(post -> post.getCreatedAt().before(accessTimeStamp) && post.getCreatedAt().after(new Date(accessTimeStamp.getTime()-(48*60*48*100))))
                .toList();

        List<String> tempIds = temp.stream()
                .map(likes::getReferenceId)
                .toList();

        tempIds = filterLikedPosts(tempIds, myId);
        query = new Query(Criteria.where("id").in(tempIds));

        return mongoCon.find(query, posts.class);
    }
}
