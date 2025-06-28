package com.fyp.fitRoute.posts.Controllers;

import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.inventory.Utilities.Response;
import com.fyp.fitRoute.notifications.Services.notificationService;
import com.fyp.fitRoute.posts.Entity.comments;
import com.fyp.fitRoute.posts.Entity.posts;
import com.fyp.fitRoute.posts.Services.commentService;
import com.fyp.fitRoute.posts.Utilities.commentRequest;
import com.fyp.fitRoute.security.Entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

@RestController
@RequestMapping("/comments")
@Tag(name = "Comment Controller", description = "Comment API endpoints")
@Slf4j
public class commentController {
    @Autowired
    private commentService commentService;
    @Autowired
    private userService userService;
    @Autowired
    private notificationService notifiService;

    @GetMapping
    @Operation(summary = "Get comments by post ID")
    public ResponseEntity<?> getComments(@RequestParam String postId){
        try {
            return new ResponseEntity<>(commentService.getByPostId(postId), HttpStatus.OK);
        } catch (Exception e){
            log.error("Error getting comments: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/replies")
    @Operation(summary = "Get replies to a comment by reference ID")
    public ResponseEntity<?> getCommentReplies(@RequestParam String referenceId){
        try {
            return new ResponseEntity<>(commentService.getByCommentId(referenceId), HttpStatus.OK);
        } catch (Exception e){
            log.error("Error getting replies to comment: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping
    @Operation(summary = "Add a comment to a post")
    public ResponseEntity<?> addComment(@RequestBody commentRequest request){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = userService.getProfile(authentication, "your profile not identified");
            comments comment = commentService.addComment(request, myProfile.getId());

            //find the post owner

            User postOwner = commentService.getPostOwner(request.getPostId());

            // Notify the user who owns the post

            notifiService.deliverNotification(
                    "Fit Route",
                    myProfile.getUsername() + " commented on your post",
                    postOwner.getUsername(),
                    myProfile.getUsername(),
                    ""
            );

            return new ResponseEntity<>(comment, HttpStatus.OK);
        } catch (Exception e){
            log.error("Error adding comment: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping
    @Operation(summary = "Delete a comment")
    public ResponseEntity<?> deleteComment(@NotNull comments comment){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = userService.getProfile(authentication, "your profile not identified");
            if (!Objects.equals(comment.getAccountId(), myProfile.getId()))
                throw new RuntimeException("You cannot delete this post");
            commentService.deleteComment(comment);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            log.error("Error deleting comment: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }
}
