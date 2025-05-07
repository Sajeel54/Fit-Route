package com.fyp.fitRoute.posts.Controllers;

import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.posts.Entity.comments;
import com.fyp.fitRoute.posts.Services.commentService;
import com.fyp.fitRoute.posts.Utilities.commentRequest;
import com.fyp.fitRoute.security.Entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity<?> getComments(@RequestParam String postId){
        try {
            return new ResponseEntity<>(commentService.getByPostId(postId), HttpStatus.OK);
        } catch (Exception e){
            log.error("Error getting comments: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/replies")
    public ResponseEntity<?> getCommentReplies(@RequestParam String referenceId){
        try {
            return new ResponseEntity<>(commentService.getByCommentId(referenceId), HttpStatus.OK);
        } catch (Exception e){
            log.error("Error getting replies to comment: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping
    public ResponseEntity<?> addComment(@RequestBody commentRequest request){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = userService.getProfile(authentication, "your profile not identified");
            comments comment = commentService.addComment(request, myProfile.getId());
            return new ResponseEntity<>(comment, HttpStatus.OK);
        } catch (Exception e){
            log.error("Error adding comment: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping
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
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
