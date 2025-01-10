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
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public ResponseEntity<?> startFollowing(@RequestParam String username){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Profile not found");

            Optional<User> followed = uService.getUserByName(username);

            if (followed.isEmpty())
                throw new Exception("user unable of being followed");
            follows entry = new follows();

            User followedData = followed.get();

            if (followedData.getId() == myProfile.getId())
                throw new Exception("user unable of being followed");

            entry.setFollowing(myProfile.getId());
            entry.setFollowed(followedData.getId());
            myProfile.setFollowings(((myProfile.getFollowings())+1));
            followedData.setFollowers(((followedData.getFollowers())+1));
            uService.addUser(myProfile);
            uService.addUser(followedData);
            follows follow =  flwService.addFollow(entry);

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
    @Transactional
    public ResponseEntity<?> unfollowUser(@RequestParam String username){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String name = authentication.getName();

            Optional<User> myProfile = uService.getUserByName(name);
            Optional<User> followed = uService.getUserByName(username);
            if (myProfile.isEmpty())
                throw new Exception("your profile not identified");

            if (followed.isEmpty())
                throw new Exception("user unable of being followed");

            User follower = myProfile.get();
            User followedData = followed.get();

            follower.setFollowings(((follower.getFollowings())-1));
            followedData.setFollowers(((followedData.getFollowers())-1));
            uService.addUser(follower);
            uService.addUser(followedData);
            boolean checkDeletion =  flwService.deleteFollow(followedData.getId(), follower.getId());
            return new ResponseEntity<>(checkDeletion, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
