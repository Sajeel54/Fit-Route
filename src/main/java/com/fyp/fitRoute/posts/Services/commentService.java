package com.fyp.fitRoute.posts.Services;

import com.fyp.fitRoute.posts.Entity.comments;
import com.fyp.fitRoute.posts.Entity.likes;
import com.fyp.fitRoute.posts.Entity.posts;
import com.fyp.fitRoute.posts.Repositories.commentRepo;
import com.fyp.fitRoute.posts.Repositories.postRepo;
import com.fyp.fitRoute.posts.Utilities.commentRequest;
import com.fyp.fitRoute.posts.Utilities.commentResponse;
import com.fyp.fitRoute.posts.Utilities.likeResponse;
import com.fyp.fitRoute.security.Entity.User;
import com.google.common.base.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class commentService {
    @Autowired
    private commentRepo commentRepo;
    @Autowired
    private postRepo postRepo;
    @Autowired
    private MongoTemplate mongoTemplate;

    public boolean checkLike(String referenceId, String myId){
        Query query = new Query();
        query.addCriteria(Criteria.where("referenceId").is(referenceId));
        query.addCriteria(Criteria.where("accountId").is(myId));
        likes like = mongoTemplate.findOne(query,likes.class);
        return like != null;
    }


    public List<commentResponse> getByPostId(String postId){
        // Fetch all comments associated with the given postId
        List<comments> commentsList = commentRepo.findByPostId(postId).stream()
                .filter(comment -> Objects.equal(comment.getReferenceId(), null))
                .toList();

        // Extract account IDs from the comments list
        List<String> accountIds = commentsList.stream()
                .map(comments::getAccountId)
                .toList();

        // Fetch all users in one query
        List<User> users = mongoTemplate.find(
                new Query(Criteria.where("id").in(accountIds)),
                User.class
        );

        // Map users by their IDs for quick lookup
        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // Build the commentResponse list
        return commentsList.stream()
                .map(comment -> {
                    User user = userMap.get(comment.getAccountId());
                    if (user == null)
                        throw new RuntimeException("User does not exist for account ID: " + comment.getAccountId());
                    return new commentResponse(
                            comment.getId(),
                            user.getUsername(),
                            user.getImage(),
                            comment.getPostId(),
                            comment.getBody(),
                            comment.getLikes(),
                            checkLike(comment.getReferenceId(), user.getId()),
                            comment.getCreatedAt(),
                            comment.getUpdatedAt()
                    );
                })
                .toList();
    }

    public List<commentResponse> getByCommentId(String commentId){
        // Fetch all comments associated with the given postId
        List<comments> commentsList = commentRepo.findByReferenceId(commentId);

        // Extract account IDs from the comments list
        List<String> accountIds = commentsList.stream()
                .map(comments::getAccountId)
                .toList();

        // Fetch all users in one query
        List<User> users = mongoTemplate.find(
                new Query(Criteria.where("id").in(accountIds)),
                User.class
        );

        // Map users by their IDs for quick lookup
        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // Build the commentResponse list
        return commentsList.stream()
                .map(comment -> {
                    User user = userMap.get(comment.getAccountId());
                    if (user == null)
                        throw new RuntimeException("User does not exist for account ID: " + comment.getAccountId());
                    return new commentResponse(
                            comment.getId(),
                            user.getUsername(),
                            user.getImage(),
                            comment.getPostId(),
                            comment.getBody(),
                            comment.getLikes(),
                            checkLike(comment.getReferenceId(), user.getId()),
                            comment.getCreatedAt(),
                            comment.getUpdatedAt()
                    );
                })
                .toList();
    }


    @Transactional
    public comments addComment(commentRequest request, String myId){
        Date date = Date.from(Instant.now());
        comments newComment = new comments(
                null, request.getReferenceId(), myId,
                request.getPostId(), request.getBody(), 0, date, date);
        Optional<posts> post = postRepo.findById(request.getPostId());
        if (post.isEmpty())
            throw new RuntimeException("Post does not exists");
        posts found = post.get();
        found.setComments(found.getComments()+1);
        postRepo.save(found);
        return commentRepo.save(newComment);
    }

    @Transactional
    public void deleteComment(comments comment){
        Optional<posts> post = postRepo.findById(comment.getPostId());
        if (post.isEmpty())
            throw new RuntimeException("Unable to delete comment");
        posts foundPost = post.get();
        foundPost.setComments(foundPost.getComments()-1);
        postRepo.save(foundPost);
        Optional<comments> found = commentRepo.findById(comment.getId());
        if (found.isPresent())
            throw new RuntimeException("Unable to delete comment");
    }
}
