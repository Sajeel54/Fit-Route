package com.fyp.fitRoute.accounts.controllers;

import com.fyp.fitRoute.accounts.Entity.follows;
import com.fyp.fitRoute.accounts.Services.followsService;
import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.security.Entity.UserCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class followsController {

    @Autowired
    private followsService flwService;
    @Autowired
    private userService uService;

    @PostMapping("/follow")
    public ResponseEntity<?> startFollowing(@RequestParam String username){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String name = authentication.getName();

            Optional<UserCredentials> myProfile = uService.getUserByName(name);
            Optional<UserCredentials> followed = uService.getUserByName(username);
            if (myProfile.isEmpty())
                throw new Exception("your profile not identified");

            if (followed.isEmpty())
                throw new Exception("user unable of being followed");

            follows entry = new follows();

            entry.setFollowing(myProfile.get().getId());
            entry.setFollowed(followed.get().getId());
            follows follow =  flwService.addFollow(entry);
            return new ResponseEntity<>(follow, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/unfollow")
    public ResponseEntity<?> unfollowUser(@RequestParam String username){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String name = authentication.getName();

            Optional<UserCredentials> myProfile = uService.getUserByName(name);
            Optional<UserCredentials> followed = uService.getUserByName(username);
            if (myProfile.isEmpty())
                throw new Exception("your profile not identified");

            if (followed.isEmpty())
                throw new Exception("user unable of being followed");


            UserCredentials profile = myProfile.get();
            UserCredentials displayUser = followed.get();

            boolean checkDeletion =  flwService.deleteFollow(followed.get().getId(), myProfile.get().getId());
            return new ResponseEntity<>(checkDeletion, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
