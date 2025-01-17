package com.fyp.fitRoute.accounts.Controllers;

import com.fyp.fitRoute.accounts.Entity.follows;
import com.fyp.fitRoute.accounts.Services.followsService;
import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.notifications.Services.notificationService;
import com.fyp.fitRoute.security.Entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag( name = "Follow Controller" )
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

//            notificationService.deliverNotification(
//                    "Fit Route",
//                    myProfile.getUsername() + "Started following you",
//                    followedData.getUsername(),
//                    myProfile.getUsername(),
//                    ""
//            );
            return new ResponseEntity<>(follow, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/unfollow")
    @Operation( summary = "Unfollow some particular user you are following" )
    public ResponseEntity<?> unfollowUser(@RequestParam String username){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String name = authentication.getName();

            User myProfile = uService.getProfile(authentication, "your profile not identified");
            Optional<User> followed = uService.getUserByName(username);

            if (followed.isEmpty())
                throw new Exception("user unable of being followed");

            boolean checkDeletion =  flwService.deleteFollow(myProfile, followed.get());
            return new ResponseEntity<>(checkDeletion, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
