package com.fyp.fitRoute.posts.Controllers;

import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.inventory.Utilities.Response;
import com.fyp.fitRoute.notifications.Services.notificationService;
import com.fyp.fitRoute.posts.Entity.likes;
import com.fyp.fitRoute.posts.Services.likeService;
import com.fyp.fitRoute.posts.Utilities.likeResponse;
import com.fyp.fitRoute.security.Entity.User;
import com.google.firebase.messaging.FirebaseMessagingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/likes")
@Tag(name = "Like Controller", description = "Like API endpoints")
@Slf4j
public class likeController {
    @Autowired
    private likeService likeService;
    @Autowired
    private userService userService;
    @Autowired
    private notificationService notifiService;

    @GetMapping
    @Operation(summary = "Get likes by postId")
    public ResponseEntity<?> getLikes(@RequestParam String referenceId){
        try {
            List<likeResponse> likes = likeService.getByPostId(referenceId);
            return new ResponseEntity<>(
                    likes,
                    HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting likes: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/check-like")
    @Operation(summary = "Check if user liked the post")
    public ResponseEntity<?> checkLike(@RequestParam String referenceId){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = userService.getProfile(authentication, "your profile not identified");
            boolean like = likeService.checkLike(referenceId, myProfile.getId());
            return new ResponseEntity<>(like, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error checking like: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    @Operation(summary = "Add like to post")
    public ResponseEntity<?> addLike(@RequestParam String postId){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = userService.getProfile(authentication, "your profile not identified");
            likes like = likeService.addLike(postId, myProfile.getId());

            //find the post owner
            User postOwner = likeService.getPostOwner(postId);

            // Notify the post owner
            notifiService.deliverNotification(
                    "Fit Route",
                    myProfile.getUsername() + " liked your post",
                    postOwner.getUsername(),
                    myProfile.getUsername(),
                    ""
            );

            return new ResponseEntity<>(like, HttpStatus.OK);
        } catch (FirebaseMessagingException e) {
            log.error("Error sending notification: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        } catch (Exception e){
            log.error("Error adding like: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
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
            log.error("Error adding like to comment: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
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
            log.error("Error deleting like: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
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
            log.error("Error deleting like from comment: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }
}
