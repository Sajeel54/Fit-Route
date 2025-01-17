package com.fyp.fitRoute.posts.Controllers;

import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.posts.Entity.posts;
import com.fyp.fitRoute.posts.Entity.route;
import com.fyp.fitRoute.posts.Services.postService;
import com.fyp.fitRoute.posts.Services.routeService;
import com.fyp.fitRoute.posts.Utilities.postRequest;
import com.fyp.fitRoute.security.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;

@RestController
@RequestMapping("/post")
public class postController {
    @Autowired
    private postService pService;
    @Autowired
    private routeService rService;
    @Autowired
    private userService uService;

    @PostMapping
    public ResponseEntity<?> addPost(
            @RequestBody postRequest body
    ){
        try{
            Date date = Date.from(Instant.now());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Your Profile not identified");

            route newRoute = rService.add(new route(
                    null, body.getDistance(), body.getTime(), body.getCoordinates(), date, date
            ));

            posts newPost = pService.addPost(new posts(
                    null, 0, 0, newRoute.getId(), myProfile.getId(),
                    body.getDescription(), body.getTags(), body.getImages(),body.getCategory(), date, date
            ));

            return new ResponseEntity<>(newPost, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
