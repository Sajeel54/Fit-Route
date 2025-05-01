package com.fyp.fitRoute.posts.Services;

import com.fyp.fitRoute.posts.Entity.comments;
import com.fyp.fitRoute.posts.Entity.likes;
import com.fyp.fitRoute.posts.Entity.posts;
import com.fyp.fitRoute.posts.Repositories.commentRepo;
import com.fyp.fitRoute.posts.Repositories.likeRepo;
import com.fyp.fitRoute.posts.Repositories.postRepo;
import com.fyp.fitRoute.posts.Utilities.likeResponse;
import com.fyp.fitRoute.security.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class likeService {
    @Autowired
    private likeRepo likeRepo;
    @Autowired
    private postRepo postRepo;
    @Autowired
    private commentRepo commentRepo;
    @Autowired
    private MongoTemplate mongoTemplate;

    public List<likeResponse> getByPostId(String reference){
        // Fetch all likes associated with the given postId
        List<likes> likeList = likeRepo.findByReferenceId(reference);

        // Extract account IDs from the likes list
        List<String> accountIds = likeList.stream()
                .map(likes::getAccountId)
                .toList();

        // Fetch all users in one query
        List<User> users = mongoTemplate.find(
                new Query(Criteria.where("id").in(accountIds)),
                User.class
        );

        // Map users by their IDs for quick lookup
        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // Build the LikeResponse list
        return likeList.stream()
                .map(like -> {
                    User user = userMap.get(like.getAccountId());
                    if (user == null)
                        throw new RuntimeException("User does not exist for account ID: " + like.getAccountId());
                    return new likeResponse(
                            like.getId(),
                            user.getUsername(),
                            user.getImage(),
                            like.getReferenceId(),
                            like.getCreatedAt(),
                            like.getUpdatedAt()
                    );
                })
                .toList();
    }

    @Transactional
    public likes addLike(String referenceId, String myId){
        Date date = Date.from(Instant.now());
        likes newLike = new likes(null, myId, referenceId, date, date);
        Optional<likes> like = likeRepo.findByReferenceIdAndAccountId(referenceId, myId);
        if (like.isPresent())
            throw new RuntimeException("You have already liked this post");
        Optional<posts> post = postRepo.findById(referenceId);
        if (post.isEmpty())
            throw new RuntimeException("Post does not exists");
        posts found = post.get();
        found.setLikes(found.getLikes()+1);
        postRepo.save(found);
        return likeRepo.save(newLike);
    }

    @Transactional
    public likes addLikeToComments(String referenceId, String myId){
        Date date = Date.from(Instant.now());
        likes newLike = new likes(null, myId, referenceId, date, date);
        Optional<comments> comment = commentRepo.findById(referenceId);

        Optional<likes> like = likeRepo.findByReferenceIdAndAccountId(referenceId, myId);
        if (like.isPresent())
            throw new RuntimeException("You have already liked this comment");

        if (comment.isEmpty())
            throw new RuntimeException("Post does not exists");
        comments found = comment.get();
        found.setLikes(found.getLikes()+1);
        commentRepo.save(found);
        return likeRepo.save(newLike);
    }

    @Transactional
    public void deleteLike(String referenceId, String myId){
        Query query = new Query();
        query.addCriteria(Criteria.where("referenceId").is(referenceId));
        query.addCriteria(Criteria.where("accountId").is(myId));
        likes like = mongoTemplate.findAndRemove(query,likes.class);

        Optional<posts> post = postRepo.findById(referenceId);
        if (post.isEmpty())
            throw new RuntimeException("Post not found");
        posts foundPost = post.get();
        foundPost.setLikes(foundPost.getLikes()-1);
        postRepo.save(foundPost);
        Optional<likes> found = likeRepo.findById(like.getId());
        if (found.isPresent())
            throw new RuntimeException("Unable to unlike post");
    }
}
