package com.fyp.fitRoute.progress.Controllers;

import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.inventory.Utilities.Response;
import com.fyp.fitRoute.progress.Services.tracker;
import com.fyp.fitRoute.security.Entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/progress")
@Tag(name = "Progress Controller", description = "Weekly Progress API endpoints")
public class progressController {
    @Autowired
    private tracker trackerService;
    @Autowired
    private userService uService;

    @GetMapping
    @Operation(summary = "Get weekly progress by user ID")
    public ResponseEntity<?> getProgress() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User myProfile = uService.getProfile(authentication, "Your Profile not identified");
            return ResponseEntity.ok(trackerService.getProgressByUserId(myProfile.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Response("Error fetching progress: " + e.getMessage(), Date.from(Instant.now())));
        }
    }

    // check other user's progress
    @GetMapping("/user")
    @Operation(summary = "Get weekly progress by user ID")
    public ResponseEntity<?> getProgressByUserId(@RequestParam String username) {
        try {
            Optional<User> user = uService.getUserByName(username);
            if (user.isEmpty()) {
                throw new RuntimeException("User not found with username: " + username);
            }
            return ResponseEntity.ok(trackerService.getProgressByUserId(user.get().getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Response("Error fetching progress: " + e.getMessage(), Date.from(Instant.now())));
        }
    }
}
