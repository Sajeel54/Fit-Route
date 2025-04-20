package com.fyp.fitRoute.recommendations.Controllers;

import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.recommendations.Services.recommendationService;
import com.fyp.fitRoute.security.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recommendations")
public class recommendationController {
    @Autowired
    private recommendationService recommendations;

    @Autowired
    private userService uService;

    @GetMapping
    public ResponseEntity<?> getPosts(){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Profile not found");

            return new ResponseEntity<>(
                    recommendations.getRecommendations(myProfile.getId())
                    , HttpStatus.OK);
        }catch (Exception ex){
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
