package com.fyp.fitRoute.recommendations.Components;

import com.fyp.fitRoute.accounts.Entity.follows;
import com.fyp.fitRoute.posts.Entity.likes;
import com.fyp.fitRoute.posts.Entity.posts;
import com.fyp.fitRoute.posts.Entity.route;
import com.fyp.fitRoute.posts.Utilities.postResponse;
import com.fyp.fitRoute.recommendations.Utilities.Filter;
import com.fyp.fitRoute.security.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class socialFilter implements Filter {
    @Autowired
    private MongoTemplate mongoCon;

    private Date accessTimeStamp;

    private List<String> postIds;

    public socialFilter(MongoTemplate mongoCon){
        this.mongoCon = mongoCon;
    }

    @Override
    public void addPostIds(List<String> postIds){ this.postIds = postIds; }

    @Override
    public void setTimeStamp(Date accessTimeStamp){ this.accessTimeStamp = accessTimeStamp;  }

    private List<String> getFollowedUsers(String myId){
        Query query = new Query(Criteria.where("following").is(myId));
        return mongoCon.find(query, follows.class).stream()
                .map(follows::getFollowed)
                .toList();
    }

    private List<String> getLikes(List<String> ids){
        Query query = new Query(Criteria.where("accountId").in(ids));
        List<likes> temp = mongoCon.find(query, likes.class).stream()
                .filter(like -> like.getCreatedAt().after(accessTimeStamp))
                .toList();

        return temp.stream()
                .map(likes::getReferenceId).toList();
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

    @Override
    public List<postResponse> getPosts(String myId){
        List<postResponse> responseList = new ArrayList<>();
        List<String> temp = getFollowedUsers(myId);
        temp = getLikes(temp);

        List<String> ids = filterLikedPosts(temp, myId);
        if (ids.isEmpty()) return responseList;

        Query query = new Query(Criteria.where("id").in(ids));
        mongoCon.find(query, posts.class)
                .forEach(post -> {
                    if (!(postIds.contains(post.getId()))) {
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
                });

        return responseList;
    }

    @Override
    public List<String> getSentPosts(){ return postIds; }
}
