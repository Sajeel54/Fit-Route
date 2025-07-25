package com.fyp.fitRoute.posts.Services;

import com.fyp.fitRoute.accounts.Entity.follows;
import com.fyp.fitRoute.inventory.Services.cloudinaryService;
import com.fyp.fitRoute.inventory.Services.redisService;
import com.fyp.fitRoute.posts.Entity.comments;
import com.fyp.fitRoute.posts.Entity.likes;
import com.fyp.fitRoute.posts.Entity.posts;
import com.fyp.fitRoute.posts.Entity.route;
import com.fyp.fitRoute.posts.Repositories.postRepo;
import com.fyp.fitRoute.posts.Repositories.routeRepo;
import com.fyp.fitRoute.posts.Utilities.postRequest;
import com.fyp.fitRoute.posts.Utilities.postResponse;
import com.fyp.fitRoute.progress.Entity.progress;
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

@Service
public class postService {
    @Autowired
    private postRepo pRepo;
    @Autowired
    private cloudinaryService cloudinaryService;
    @Autowired
    private routeRepo rRepo;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private redisService redisService;

    public boolean checkLike(String referenceId, String myId){
        Query query = new Query();
        query.addCriteria(Criteria.where("referenceId").is(referenceId));
        query.addCriteria(Criteria.where("accountId").is(myId));
        likes like = mongoTemplate.findOne(query,likes.class);
        return like != null;
    }

    public List<postResponse> getFollowingsPosts(String myId, String username) throws RuntimeException{
        Date date = Date.from(Instant.now());

        Date accessTimeStamp = Objects.requireNonNullElseGet(
                redisService.get("News Feed Access " + myId, Date.class),
                () -> Date.from(Instant.now().minusSeconds(48L * 60L * 60L))
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
        Map<String, postResponse> postList = new HashMap<>();
         mongoTemplate.find(query, posts.class)
                .forEach(post -> {
                    if (!(postList.containsKey(post.getId()))) {
                        User user = mongoTemplate.findOne(new Query(Criteria.where("id").is(post.getAccountId())), User.class);
                        if (user == null)
                            throw new RuntimeException("User not found");
                        postResponse response = new postResponse(
                                post.getId(),post.getTitle(), post.getLikes(), post.getComments(),
                                user.getUsername(), user.getImage(), post.getDescription(),
                                post.getTags(), post.getImages(), post.getCategory(),
                                post.getCreatedAt(), post.getUpdatedAt(),
                                checkLike(post.getId(), myId),
                                mongoTemplate.findOne(
                                        new Query(Criteria.where("").is(post.getRouteId())), route.class
                                )
                        );
                        postList.put(response.getId(), response);
                    }
                });
        for (String accountId : recentFollowingIds) {
            query = new Query(Criteria.where("accountId").is(accountId))
                    .with(Sort.by(Sort.Direction.DESC, "createdAt"))
                    .limit(2);

            mongoTemplate.find(query, posts.class)
                    .forEach(post -> {
                        if (!(postList.containsKey(post.getId()))) {
                            User user = mongoTemplate.findOne(new Query(Criteria.where("id").is(post.getAccountId())), User.class);
                            if (user == null)
                                throw new RuntimeException("User not found");
                            postResponse response = new postResponse(
                                    post.getId(), post.getTitle(), post.getLikes(), post.getComments(),
                                    user.getUsername(), user.getImage(), post.getDescription(),
                                    post.getTags(), post.getImages(), post.getCategory(),
                                    post.getCreatedAt(), post.getUpdatedAt(),
                                    checkLike(post.getId(), myId),
                                    mongoTemplate.findOne(
                                            new Query(Criteria.where("").is(post.getRouteId())), route.class
                                    )
                            );
                            postList.put(response.getId(), response);
                        }
                    });

            }
        List<postResponse> responseList = new ArrayList<>();
        postList.forEach((k,v) -> responseList.add(v));
        return responseList;
    }

    public List<postResponse> getUserPosts(String accountId){
        User user = mongoTemplate.findOne(new Query(Criteria.where("id").is(accountId)), User.class);
        if (user == null)
            throw new RuntimeException("User not found");
        return pRepo.findByAccountId(accountId)
                .stream()
                .map(post -> {
                    return new postResponse(
                            post.getId(), post.getTitle(),post.getLikes(), post.getComments(),
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
    public posts addPost(postRequest body, String myId) throws IOException {
        Date date = Date.from(Instant.now());
        route newRoute = rRepo.save(new route(
                null, body.getDistance(), body.getTime(), body.getCoordinates(), date, date
        ));

        posts newPost = pRepo.save(new posts(
                null, body.getTitle(),0, 0, newRoute.getId(), myId,
                body.getDescription(), body.getTags(), body.getImages(),body.getCategory(), date, date
        ));
        List<String> imageUrls = new ArrayList<>();

        for (String image : newPost.getImages()){
            int index = 0;
            imageUrls.add(
                    cloudinaryService.uploadImage(
                            image,
                            newPost.getId()+"index"+index,
                            false
                    )
            );
            index++;
        }
        Query query = new Query(Criteria.where("id").is(myId));
        User user = mongoTemplate.findOne(query, User.class);
        if (user == null)
            throw new RuntimeException("User not found");
        user.setActivities(user.getActivities()+1);
        mongoTemplate.save(user);

        //update progress of user
        query = new Query(Criteria.where("userId").is(myId));
        progress userProgress = mongoTemplate.findOne(query, progress.class);
        if (userProgress == null) {
            userProgress = new progress();
            userProgress.setUserId(myId);
        }
        double[] dailyDistance = userProgress.getDailyDistance();
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        dailyDistance[dayOfWeek] += body.getDistance();
        userProgress.setDailyDistance(dailyDistance);
        mongoTemplate.save(userProgress);

        newPost.setImages(imageUrls);
        return pRepo.save(newPost);
    }

    @Transactional
    public void deletePost(String publisherId, String postId){
        Optional<posts> selectedPost = pRepo.findById(postId);

        if (selectedPost.isEmpty())
            throw new RuntimeException("Post Not found");

        posts post = selectedPost.get();

        if (!Objects.equals(post.getAccountId(), publisherId))
            throw new RuntimeException("Unable to delete post");

        Query query = new Query(Criteria.where("postId").is(post.getId()));
        mongoTemplate.findAllAndRemove(query, likes.class);
        mongoTemplate.findAllAndRemove(query, comments.class);
        User user = mongoTemplate.findOne(new Query(Criteria.where("id").is(publisherId)), User.class);
        if (user == null)
            throw new RuntimeException("User not found");

        user.setActivities(user.getActivities()-1);
        mongoTemplate.save(user);

        rRepo.deleteById(post.getRouteId());
        Optional<route> foundRoute = rRepo.findById(post.getRouteId());
        pRepo.delete(post);
        Optional<posts> foundPost = pRepo.findById(post.getId());
        if (foundPost.isPresent() || foundRoute.isPresent())
            throw new RuntimeException("Post Not deleted");
    }
}
