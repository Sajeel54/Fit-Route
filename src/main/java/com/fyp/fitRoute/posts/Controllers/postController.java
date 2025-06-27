package com.fyp.fitRoute.posts.Controllers;

import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.inventory.Utilities.Response;
import com.fyp.fitRoute.posts.Entity.posts;
import com.fyp.fitRoute.posts.Services.postService;
import com.fyp.fitRoute.posts.Utilities.postRequest;
import com.fyp.fitRoute.posts.Utilities.postResponse;
import com.fyp.fitRoute.security.Entity.User;
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
@RequestMapping("/post")
@Tag(name = "Post Controller", description = "Post API endpoints")
@Slf4j
public class postController {
    @Autowired
    private postService pService;
    @Autowired
    private userService uService;

    @GetMapping
    @Operation(summary = "Get posts by accountId")
    public ResponseEntity<?> getPosts(@RequestParam String accountId){
        try {
            return new ResponseEntity<>(
                    pService.getUserPosts(accountId),
                    HttpStatus.OK);
        } catch (Exception e){
            log.error("Error getting posts: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/news")
    @Operation(summary = "Get posts by followings")
    public ResponseEntity<?> getYourNewsFeed(){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Your Profile not identified");

            List<postResponse> posts = pService.getFollowingsPosts(myProfile.getId(), myProfile.getUsername());

            return new ResponseEntity<>(posts,HttpStatus.OK);
        } catch (Exception e){
            log.error("Error getting news feed: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/myPosts")
    @Operation(summary = "Get your posts")
    public ResponseEntity<?> getYourPosts(){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Your Profile not identified");

            return new ResponseEntity<>(
                    pService.getUserPosts(myProfile.getId()),
                    HttpStatus.OK);
        } catch (Exception e){
            log.error("Error getting your posts: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    @Operation(summary = "Add a post")
    public ResponseEntity<?> addPost(
            @RequestBody postRequest body
    ){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Your Profile not identified");

            posts newPost = pService.addPost(body, myProfile.getId());

            return new ResponseEntity<>(newPost, HttpStatus.OK);
        } catch (Exception e){
            log.error("Error adding post: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping
    @Operation(summary = "Delete a post")
    public ResponseEntity<?> deletePost(@RequestParam String postId){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Your Profile not identified");
            pService.deletePost(myProfile.getId(), postId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            log.error("Error deleting post: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }
}
