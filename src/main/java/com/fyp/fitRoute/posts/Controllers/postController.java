package com.fyp.fitRoute.posts.Controllers;

import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.posts.Entity.posts;
import com.fyp.fitRoute.posts.Services.postService;
import com.fyp.fitRoute.posts.Utilities.postRequest;
import com.fyp.fitRoute.posts.Utilities.postResponse;
import com.fyp.fitRoute.security.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/post")
public class postController {
    @Autowired
    private postService pService;
    @Autowired
    private userService uService;

    @GetMapping
    public ResponseEntity<?> getPosts(@RequestParam String accountId){
        try {
            return new ResponseEntity<>(
                    pService.getUserPosts(accountId),
                    HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/news")
    public ResponseEntity<?> getYourNewsFeed(){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Your Profile not identified");

            List<postResponse> posts = pService.getFollowingsPosts(myProfile.getId(), myProfile.getUsername());

            return new ResponseEntity<>(posts,HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/myPosts")
    public ResponseEntity<?> getYourPosts(){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Your Profile not identified");

            return new ResponseEntity<>(
                    pService.getUserPosts(myProfile.getId()),
                    HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    public ResponseEntity<?> addPost(
            @RequestBody postRequest body
    ){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Your Profile not identified");

            posts newPost = pService.addPost(body, myProfile.getId());

            return new ResponseEntity<>(newPost, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deletePost(@RequestParam String postId){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Your Profile not identified");
            pService.deletePost(myProfile.getId(), postId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
