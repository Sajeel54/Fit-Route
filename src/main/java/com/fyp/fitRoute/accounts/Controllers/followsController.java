package com.fyp.fitRoute.accounts.Controllers;

import com.fyp.fitRoute.accounts.Entity.follows;
import com.fyp.fitRoute.accounts.Entity.profileCard;
import com.fyp.fitRoute.accounts.Services.followsService;
import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.inventory.Utilities.Response;
import com.fyp.fitRoute.notifications.Services.notificationService;
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
import java.util.Optional;

@RestController
@Tag( name = "Follow Controller" )
@Slf4j
public class followsController {

    @Autowired
    private followsService flwService;
    @Autowired
    private userService uService;
    @Autowired
    private notificationService notificationService;

    @PostMapping("/follow")
    @Operation( summary = "Follow someone" )
    public ResponseEntity<?> startFollowing(@RequestParam String username){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Profile not found");

            Optional<User> followed = uService.getUserByName(username);
            if (followed.isEmpty())
                throw new RuntimeException("user unable of being followed");
            follows follow =  flwService.addFollow(myProfile, followed.get());

            notificationService.deliverNotification(
                    "Fit Route",
                    myProfile.getUsername() + " started following you",
                    followed.get().getUsername(),
                    myProfile.getUsername(),
                    ""
            );
            return new ResponseEntity<>(follow, HttpStatus.OK);
        }catch (FirebaseMessagingException e) {
            log.error("Error sending notification: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error following user: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/unfollow")
    @Operation( summary = "Unfollow some particular user you are following" )
    public ResponseEntity<?> unfollowUser(@RequestParam String username){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            User myProfile = uService.getProfile(authentication, "your profile not identified");
            Optional<User> followed = uService.getUserByName(username);

            if (followed.isEmpty())
                throw new Exception("user unable of being followed");

            boolean checkDeletion =  flwService.deleteFollow(myProfile, followed.get());
            return new ResponseEntity<>(checkDeletion, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error unfollowing user: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/followers")
    @Operation( summary = "Get followers of a user" )
    public ResponseEntity<?> getFollowers(@RequestParam String id){
        try{
            return new ResponseEntity<>(
                    flwService.getFollowers(id),
                    HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error getting followers: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/followings")
    @Operation( summary = "Get users followed by any user" )
    public ResponseEntity<?> getFollowings(@RequestParam String id){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Profile not found");
            List<profileCard> users = flwService.getFollowing(id).stream()
                    .peek(user-> user.setFollow(flwService.checkFollow(myProfile.getId(), user.getId()))).toList();
            return new ResponseEntity<>(
                    users,
                    HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error getting followings: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.NO_CONTENT);
        }
    }
}
