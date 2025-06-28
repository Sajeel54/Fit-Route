package com.fyp.fitRoute.notifications.Controllers;

import com.fyp.fitRoute.inventory.Utilities.Response;
import com.fyp.fitRoute.notifications.Entity.Notification;
import com.fyp.fitRoute.notifications.Services.notificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;

@RestController
@RequestMapping("/notification")
@Tag( name = "Notification Controller")
public class notificationController {
    @Autowired
    private notificationService notificationService;


    @PostMapping("/register-token")
    @Operation( summary = "Register your fcm token for notifications service" )
    public ResponseEntity<?> registerToken(@RequestParam String token){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            notificationService.registerToken(authentication.getName(), token);

            return new ResponseEntity<>("Registered", HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/refresh-token")
    @Operation( summary = "Register your fcm token for notifications service" )
    public ResponseEntity<?> refreshToken(@RequestParam String newToken, @RequestParam String oldToken){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            notificationService.registerToken(authentication.getName(), newToken);

            return new ResponseEntity<>("Registered", HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get-notifications")
    @Operation( summary = "Get notifications for the user" )
    public ResponseEntity<?> getNotifications(@RequestParam String username){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String myUsername = authentication.getName();

            if (!myUsername.equals(username)) {
                return new ResponseEntity<>(new Response("Unauthorized access", Date.from(Instant.now())), HttpStatus.UNAUTHORIZED);
            }

            return new ResponseEntity<>(notificationService.getNotifications(username), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }
}
