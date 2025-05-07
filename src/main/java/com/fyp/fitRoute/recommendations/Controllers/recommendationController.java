package com.fyp.fitRoute.recommendations.Controllers;

import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.posts.Utilities.postResponse;
import com.fyp.fitRoute.recommendations.Services.recommendationService;
import com.fyp.fitRoute.security.Entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
@Tag(name="Recommendation Controller", description = "These endpoints are used to get recommendations for the user")
@Slf4j
public class recommendationController {
    @Autowired
    private recommendationService recommendations;

    @Autowired
    private userService uService;

    @GetMapping
    @Operation(summary = "Get Recommendations", description = "This endpoint is used to get recommendations for the user")
    public ResponseEntity<?> getPosts(){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Profile not found");
            List<postResponse> posts = recommendations.getRecommendations(myProfile.getId());

            return new ResponseEntity<>(
                    posts, HttpStatus.OK);
        }catch (Exception ex){
            log.error("Error occurred while fetching recommendations: {}", ex.getMessage());
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
