package com.fyp.fitRoute.accounts.controllers;

import com.fyp.fitRoute.accounts.Entity.follows;
import com.fyp.fitRoute.accounts.Services.firebaseService;
import com.fyp.fitRoute.accounts.Services.followsService;
import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.security.Entity.UserCredentials;
import com.google.api.Http;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/Profile")
public class userController {
    @Autowired
    private userService uService;

    @Autowired
    private followsService flwService;

    @Autowired
    private firebaseService frbsService;


    @PostMapping("/upload-picture")
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("image")MultipartFile profilePicture){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserCredentials user = uService.getProfile(authentication, "User not found");
            String url = frbsService.upload(profilePicture, user.getUsername());
            user.setImageUrl(url);
            UserCredentials updatedUser = uService.updateUser(user.getId(), user);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ADMIN")
    public ResponseEntity<?> getAllUsers() {
        try{
            List<UserCredentials> found = uService.getAllUsers();
            if (found.isEmpty())
                throw new Exception("Empty");
            return new ResponseEntity<>(found, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }

    @GetMapping
    public ResponseEntity<?> getUserProfile() {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserCredentials myProfile = uService.getProfile(authentication, "Your Profile not identified");
            return new ResponseEntity<>(myProfile, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }

    @GetMapping("/followers")
    public ResponseEntity<?> getFollowers(){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String name = authentication.getName();

            UserCredentials myProfile = uService.getProfile(authentication, "Your Profile not identified");

            List<follows> followers = flwService.getFollowers(myProfile.getId());
            return new ResponseEntity<>(followers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/followings")
    public ResponseEntity<?> getFollowings(){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String name = authentication.getName();

            Optional<UserCredentials> myProfile = uService.getUserByName(name);
            if (myProfile.isEmpty())
                throw new Exception("No follower found");

            List<follows> followers = flwService.getFollowing(myProfile.get().getId());
            return new ResponseEntity<>(followers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NO_CONTENT);
        }
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody UserCredentials userDetails) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String name = authentication.getName();

            Optional<UserCredentials> found = uService.getUserByName(name);
            if (found.isEmpty())
                throw new Exception("Not found");
            UserCredentials saved = uService.updateUser(found.get().getId(), userDetails);
            return new ResponseEntity<>(saved, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser() {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String name = authentication.getName();

            Optional<UserCredentials> found = uService.getUserByName(name);
            if (found.isEmpty())
                throw new Exception("Not found");
            boolean check = uService.deleteUsers(found.get().getId());
            if (check)
                return new ResponseEntity<>("Deletion successful", HttpStatus.OK);
            else
                throw new Exception("User not deleted");
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }
}
