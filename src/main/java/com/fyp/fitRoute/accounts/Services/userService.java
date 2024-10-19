package com.fyp.fitRoute.accounts.Services;

import com.fyp.fitRoute.security.Entity.UserCredentials;
import com.fyp.fitRoute.security.Repositories.userCredentialsRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class userService {
    @Autowired
    private userCredentialsRepo userRepo;

    public List<UserCredentials> getAllUsers() {
        return userRepo.findAll();
    }

    public Optional<UserCredentials> getUserByName(String name) {
        return userRepo.findByUsername(name);
    }

    public UserCredentials addUser(UserCredentials user) {
        user.setRole("USER");
        user.setCreatedAt(Date.from(Instant.now()));
        user.setUpdatedAt(user.getCreatedAt());
        return userRepo.save(user);
    }

    public UserCredentials updateUser(String id, UserCredentials userDetails) {
        UserCredentials user = userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUpdatedAt(Date.from(Instant.now()));
        user.setUsername(userDetails.getUsername());
        user.setPassword(userDetails.getPassword());
        user.setEmail(userDetails.getEmail());
        user.setDob(userDetails.getDob());
        user.setBio(userDetails.getBio());
        return userRepo.save(user);
    }

    public boolean deleteUsers(String id) {
        UserCredentials user = userRepo.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));
        userRepo.delete(user);
        Optional<UserCredentials> checker = userRepo.findById(id);

        return checker.isEmpty();
    }
}
