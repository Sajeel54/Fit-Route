package com.fyp.fitRoute.accounts.Controllers;

import com.fyp.fitRoute.accounts.Entity.profile;
import com.fyp.fitRoute.accounts.Entity.profileCard;
import com.fyp.fitRoute.accounts.Utilities.UserDto;
import com.fyp.fitRoute.accounts.Utilities.profileRequest;
import com.fyp.fitRoute.accounts.Services.followsService;
import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.inventory.Utilities.Response;
import com.fyp.fitRoute.inventory.Utilities.numericResponse;
import com.fyp.fitRoute.security.Entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/Profile")
@Tag( name = "User Controller" , description = "Endpoints for profile managements")
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class userController {
    @Autowired
    private userService uService;

    @Autowired
    private followsService flwService;

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
            log.error("Error getting all users: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
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
            log.error("Error getting user profile: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);

        }
    }

    @GetMapping("/myFollowers")
    @Operation( summary = "Get your followers" )
    public ResponseEntity<?> getMyFollowers(){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            User myProfile = uService.getProfile(authentication, "Your Profile not identified");

            return new ResponseEntity<>(
                    flwService.getFollowers(myProfile.getId()),
                    HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error getting followers: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/myFollowings")
    @Operation( summary = "Get users whom you follow" )
    public ResponseEntity<?> getMyFollowings(){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Profile not found");

            return new ResponseEntity<>(
                    flwService.getFollowing(myProfile.getId()),
                    HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error getting followings: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.NO_CONTENT);
        }
    }

    @PostMapping("/createProfile")
    @Operation( summary="set up profile after signup" )
    public ResponseEntity<?> setUpProfile(@RequestBody profileRequest pRequest){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            User myProfile = uService.getProfile(authentication, "Your Profile not identified");
            uService.setUpAccount(pRequest ,myProfile);

            return new ResponseEntity<>("Profile created successfully!", HttpStatus.OK);
        } catch (Exception e){
            log.error("Error setting up profile: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping
    @Operation( summary = "Update your profile" )
    public ResponseEntity<?> updateUser(@RequestBody UserDto userDetails) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Your profile not found");
            User saved = uService.updateUser(myProfile.getId(), userDetails);
            return new ResponseEntity<>(saved, HttpStatus.OK);
        } catch (Exception e){
            log.error("Error updating user profile: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
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
            log.error("Error deleting user profile: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/search/users")
    @Operation( summary = "Search users" )
    public ResponseEntity<?> searchUsers(@RequestParam("u") String username){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Profile not found.");
            List<profileCard> users = uService.getProfileCard(username);
            users.stream()
                    .peek(user -> {
                        flwService.checkFollow(myProfile.getId(), user.getId());
                    }).toList();

            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e){
            log.error("Error searching users: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/followed/search")
    @Operation( summary = "Search followed users" )
    public ResponseEntity<?> searchFollowedUser(@RequestParam("u") String username){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Profile not found.");
            List<profileCard> users = uService.getProfileCardOfFollowed(username, myProfile.getId());

            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e){
            log.error("Error searching followed users: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/search")
    @Operation( summary = "search profile of a user to visit" )
    public ResponseEntity<?> getUserProfile(@RequestParam("u") String username){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Profile not found.");
            profile u = uService.getProfilebyUsername(username);
            u.setFollow(flwService.checkFollow(myProfile.getId(), u.getId()));

            return new ResponseEntity<>(u, HttpStatus.OK);
        } catch (Exception e){
            log.error("Error getting user profile: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/All")
    @Operation( summary = "Get all users you don't follow" )
    public ResponseEntity<?> getAllProfiles() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Profile not found.");
            List<profileCard> users = uService.getProfileOfAll();
            users = users.stream()
                    .filter(user -> !(user.getId().equals(myProfile.getId())))
                    .peek(user -> user.setFollow(flwService.checkFollow(myProfile.getId(), user.getId())))
                    .toList();
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting all users: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/total-users")
    @Operation( summary = "Get total number of users" )
    public ResponseEntity<?> getTotalUsers() {
        try {
            return new ResponseEntity<>(new numericResponse((int)uService.getTotalUsers()), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting total users: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.NO_CONTENT);
        }
    }

}
