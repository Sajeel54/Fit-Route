package com.fyp.fitRoute.posts.Services;

import com.fyp.fitRoute.accounts.Entity.follows;
import com.fyp.fitRoute.inventory.Services.firebaseService;
import com.fyp.fitRoute.inventory.Services.redisService;
import com.fyp.fitRoute.posts.Entity.comments;
import com.fyp.fitRoute.posts.Entity.likes;
import com.fyp.fitRoute.posts.Entity.posts;
import com.fyp.fitRoute.posts.Entity.route;
import com.fyp.fitRoute.posts.Repositories.postRepo;
import com.fyp.fitRoute.posts.Repositories.routeRepo;
import com.fyp.fitRoute.posts.Utilities.postRequest;
import com.fyp.fitRoute.posts.Utilities.postResponse;
import com.fyp.fitRoute.posts.Utilities.postsWebsocketHandler;
import com.fyp.fitRoute.security.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class postService {
    @Autowired
    private postRepo pRepo;
    @Autowired
    private firebaseService firebaseService;
    @Autowired
    private routeRepo rRepo;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private redisService redisService;
    @Autowired
    private postsWebsocketHandler postsHandler;

    public boolean checkLike(String postId, String myId){
        Query query = new Query();
        query.addCriteria(Criteria.where("postId").is(postId));
        query.addCriteria(Criteria.where("accountId").is(myId));
        likes like = mongoTemplate.findOne(query,likes.class);
        return like != null;
    }

    public List<String> getFollowingsPosts(String myId, String username) throws RuntimeException{
        Date date = Date.from(Instant.now());
        if (!(postsHandler.checkConnection(username))){
            throw new RuntimeException("You are not connected to delivery socket");
        }

        Date accessTimeStamp = Objects.requireNonNullElseGet(
                redisService.get("News Feed Access " + myId, Date.class),
                () -> Date.from(Instant.now().minusSeconds(48 * 60 * 60))
        );
        redisService.set("News Feed Access " + myId, date, -1L);

        Query query = new Query();
        query.addCriteria(Criteria.where("following").is(myId));
        List<follows> followsList = mongoTemplate.find(query, follows.class);
        List<String> recentFollowingIds = followsList.stream()
                .filter(follow -> follow.getCreatedAt().after(accessTimeStamp))
                .map(follows::getFollowed)
                .toList();

        List<String> followingIds = followsList.stream()
                .map(follows::getFollowed)
                .toList();

        query = new Query(Criteria.where("accountId").in(followingIds));
        query.addCriteria(Criteria.where("createdAt").gte(accessTimeStamp));
        List<String> postIds = new ArrayList<>();
         mongoTemplate.find(query, posts.class)
                .forEach(post -> {
                    if (!(postIds.contains(post.getId()))) {
                        User user = mongoTemplate.findOne(new Query(Criteria.where("id").is(post.getAccountId())), User.class);
                        if (user == null)
                            throw new RuntimeException("User not found");
                        postResponse response = new postResponse(
                                post.getId(), post.getLikes(), post.getComments(),
                                user.getUsername(), user.getImage(), post.getDescription(),
                                post.getTags(), post.getImages(), post.getCategory(),
                                post.getCreatedAt(), post.getUpdatedAt(),
                                checkLike(post.getId(), myId),
                                mongoTemplate.findOne(
                                        new Query(Criteria.where("").is(post.getRouteId())), route.class
                                )
                        );
                        try {
                            postIds.add(
                                    postsHandler.sendFeed(username, response)
                            );
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        for (String accountId : recentFollowingIds) {
            query = new Query(Criteria.where("accountId").is(accountId))
                    .with(Sort.by(Sort.Direction.DESC, "createdAt"))
                    .limit(2);

            mongoTemplate.find(query, posts.class)
                    .forEach(post -> {
                        if (!(postIds.contains(post.getId()))) {
                            User user = mongoTemplate.findOne(new Query(Criteria.where("id").is(post.getAccountId())), User.class);
                            if (user == null)
                                throw new RuntimeException("User not found");
                            postResponse response = new postResponse(
                                    post.getId(), post.getLikes(), post.getComments(),
                                    user.getUsername(), user.getImage(), post.getDescription(),
                                    post.getTags(), post.getImages(), post.getCategory(),
                                    post.getCreatedAt(), post.getUpdatedAt(),
                                    checkLike(post.getId(), myId),
                                    mongoTemplate.findOne(
                                            new Query(Criteria.where("").is(post.getRouteId())), route.class
                                    )
                            );
                            try {
                                postIds.add(
                                        postsHandler.sendFeed(username, response)
                                );
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

            }
        return postIds;
    }

    public List<postResponse> getUserPosts(String accountId){
        User user = mongoTemplate.findOne(new Query(Criteria.where("id").is(accountId)), User.class);
        if (user == null)
            throw new RuntimeException("User not found");
        return pRepo.findByAccountId(accountId)
                .stream()
                .map(post -> {
                    return new postResponse(
                            post.getId(),post.getLikes(), post.getComments(),
                            user.getUsername(), user.getImage(), post.getDescription(),
                            post.getTags(), post.getImages(), post.getCategory(),
                            post.getCreatedAt(), post.getUpdatedAt(),
                            checkLike(post.getId(), accountId),
                            rRepo.findById(post.getRouteId()).orElse(null)
                    );
                })
                .toList();
    }

    @Transactional
    public posts addPost(postRequest body, String myId) {
        Date date = Date.from(Instant.now());
        route newRoute = rRepo.save(new route(
                null, body.getDistance(), body.getTime(), body.getCoordinates(), date, date
        ));

        posts newPost = pRepo.save(new posts(
                null, 0, 0, newRoute.getId(), myId,
                body.getDescription(), body.getTags(), body.getImages(),body.getCategory(), date, date
        ));
        List<String> imageUrls = new ArrayList<>();
/*
        for (String image : newPost.getImages()){
            imageUrls.add(
                    firebaseService.uploadImage(
                            image,
                            post.getId()+","+image
                    )
            );
        }
*/
        newPost.setImages(imageUrls);
        return pRepo.save(newPost);
    }

    @Transactional
    public void deletePost(String publisherId, posts post){
        if (!Objects.equals(post.getAccountId(), publisherId))
            throw new RuntimeException("Unable to delete post");

        Query query = new Query(Criteria.where("postId").is(post.getId()));
        mongoTemplate.findAllAndRemove(query, likes.class);
        mongoTemplate.findAllAndRemove(query, comments.class);

        rRepo.deleteById(post.getRouteId());
        Optional<route> foundRoute = rRepo.findById(post.getRouteId());
        pRepo.delete(post);
        Optional<posts> foundPost = pRepo.findById(post.getId());
        if (foundPost.isPresent() || foundRoute.isPresent())
            throw new RuntimeException("Post Not deleted");
    }
}
