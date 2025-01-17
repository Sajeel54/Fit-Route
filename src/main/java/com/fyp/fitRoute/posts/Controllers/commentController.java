package com.fyp.fitRoute.posts.Controllers;

import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.posts.Entity.comments;
import com.fyp.fitRoute.posts.Services.commentService;
import com.fyp.fitRoute.posts.Utilities.commentResponse;
import com.fyp.fitRoute.security.Entity.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
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
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    public ResponseEntity<?> addComment(String postId, String body){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = userService.getProfile(authentication, "your profile not identified");
            comments comment = commentService.addComment(postId, myProfile.getId(), body);
            return new ResponseEntity<>(comment, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteComment(@NotNull comments comment){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = userService.getProfile(authentication, "your profile not identified");
            if (comment.getAccountId() != myProfile.getId())
                throw new RuntimeException("You cannot delete this post");
            commentService.deleteComment(comment);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
