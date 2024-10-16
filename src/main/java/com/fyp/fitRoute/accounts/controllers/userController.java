package com.fyp.fitRoute.accounts.controllers;

import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.security.Entity.UserCredentials;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/Profile")
public class userController {
    @Autowired
    userService uService;

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
            String name = authentication.getName();

            Optional<UserCredentials> found = uService.getUserByName(name);
            if (found.isEmpty())
                throw new Exception("Not found");
            return new ResponseEntity<>(found, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable ObjectId id, @RequestBody UserCredentials userDetails) {
        try{
            UserCredentials saved = uService.updateUser(id, userDetails);
            return new ResponseEntity<>(saved, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable ObjectId id) {
        try{
            uService.deleteUsers(id);
            return new ResponseEntity<>("Deletion successful", HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }
}
