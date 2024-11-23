package com.fyp.fitRoute.accounts.Controllers;

import com.fyp.fitRoute.accounts.Entity.follows;
import com.fyp.fitRoute.accounts.Entity.profileCard;
import com.fyp.fitRoute.external_Integrations.Services.firebaseService;
import com.fyp.fitRoute.accounts.Services.followsService;
import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.security.Entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/Profile")
@Tag( name = "User Controller" , description = "Endpoints for profile managements")
public class userController {
    @Autowired
    private userService uService;

    @Autowired
    private followsService flwService;

    @Autowired
    private firebaseService frbsService;


    @PostMapping("/upload-picture")
    @Operation( summary = "upload your profile picture")
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("image")MultipartFile profilePicture){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = uService.getProfile(authentication, "User not found");
            String url = frbsService.upload(profilePicture, user.getUsername());
            user.setImageUrl(url);
            User updatedUser = uService.updateUser(user.getId(), user);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ADMIN")
    @Operation( summary = "Get All Users" )
    public ResponseEntity<?> getAllUsers() {
        try{
            List<User> found = uService.getAllUsers();
            if (found.isEmpty())
                throw new Exception("Empty");
            return new ResponseEntity<>(found, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }

    @GetMapping
    @Operation( summary = "Get your profile" )
    public ResponseEntity<?> getUserProfile() {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Your Profile not identified");
            return new ResponseEntity<>(myProfile, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }

    @GetMapping("/followers")
    @Operation( summary = "Get your followers" )
    public ResponseEntity<?> getFollowers(){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String name = authentication.getName();

            User myProfile = uService.getProfile(authentication, "Your Profile not identified");

            List<follows> followers = flwService.getFollowers(myProfile.getId());
            List<profileCard> users = new ArrayList<>();
            for (follows follower: followers){
                Optional<User> user = uService.getUserById(follower.getFollowing());
                if (user.isEmpty())
                    continue;
                profileCard profile = uService.convertToProfileCard(user.get());
                profile.setFollow(flwService.checkFollow(myProfile.getId(), profile.getId()));
                users.add(profile);
            }

            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/followings")
    @Operation( summary = "Get users whom you follow" )
    public ResponseEntity<?> getFollowings(){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Profile not found");
            List<follows> followers = flwService.getFollowing(myProfile.getId());
            List<profileCard> users = new ArrayList<>();
            for (follows follower: followers){
                Optional<User> user = uService.getUserById(follower.getFollowed());
                if (user.isEmpty())
                    continue;
                profileCard profile = uService.convertToProfileCard(user.get());
                profile.setFollow(flwService.checkFollow(myProfile.getId(), profile.getId()));
                users.add(profile);
            }

            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NO_CONTENT);
        }
    }

    @PutMapping
    @Operation( summary = "Update your profile" )
    public ResponseEntity<?> updateUser(@RequestBody User userDetails) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Your profile not found");
            User saved = uService.updateUser(myProfile.getId(), userDetails);
            return new ResponseEntity<>(saved, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }

    @DeleteMapping
    @Operation( summary = "delete your profile" )
    public ResponseEntity<?> deleteUser() {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User found = uService.getProfile(authentication, "Profile not found");
            boolean check = uService.deleteUsers(found.getId());
            if (check)
                return new ResponseEntity<>("Deletion successful", HttpStatus.OK);
            else
                throw new Exception("User not deleted");
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }

    @GetMapping("/search")
    @Operation( summary = "Search users" )
    public ResponseEntity<?> searchUsers(@RequestParam("u") String username){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Profile not found.");
            List<profileCard> users = uService.getProfileCard(username);

            for (profileCard user: users){
                user.setFollow(flwService.checkFollow(myProfile.getId(), user.getId()));
            }

            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NO_CONTENT);
        }
    }
}
