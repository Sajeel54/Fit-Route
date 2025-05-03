package com.fyp.fitRoute.posts.Controllers;

import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.posts.Entity.likes;
import com.fyp.fitRoute.posts.Services.likeService;
import com.fyp.fitRoute.posts.Utilities.likeResponse;
import com.fyp.fitRoute.security.Entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/likes")
@Tag(name = "Like Controller", description = "Like API endpoints")
public class likeController {
    @Autowired
    private likeService likeService;
    @Autowired
    private userService userService;

    @GetMapping
    @Operation(summary = "Get likes by postId")
    public ResponseEntity<?> getLikes(@RequestParam String referenceId){
        try {
            List<likeResponse> likes = likeService.getByPostId(referenceId);
            return new ResponseEntity<>(
                    likes,
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    @Operation(summary = "Add like to post")
    public ResponseEntity<?> addLike(@RequestParam String postId){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = userService.getProfile(authentication, "your profile not identified");
            likes like = likeService.addLike(postId, myProfile.getId());
            return new ResponseEntity<>(like, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/toComment")
    @Operation(summary = "Add like to comment")
    public ResponseEntity<?> addLikeToComments(@RequestParam String commentId){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = userService.getProfile(authentication, "your profile not identified");
            likes like = likeService.addLikeToComments(commentId, myProfile.getId());
            return new ResponseEntity<>(like, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping
    @Operation(summary = "Delete like from post")
    public ResponseEntity<?> deleteLike(@RequestParam String postId){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = userService.getProfile(authentication, "your profile not identified");
            likeService.deleteLike(postId, myProfile.getId());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/fromComment")
    @Operation(summary = "Delete like from comment")
    public ResponseEntity<?> deleteLikeFromComments(@RequestParam String postId){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = userService.getProfile(authentication, "your profile not identified");
            likeService.deleteLikeFromComments(postId, myProfile.getId());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
