package com.fyp.fitRoute.accounts.Services;

import com.fyp.fitRoute.accounts.Entity.follows;
import com.fyp.fitRoute.accounts.Entity.profileCard;
import com.fyp.fitRoute.security.Entity.User;
import com.fyp.fitRoute.security.Repositories.userCredentialsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class userService {
    @Autowired
    private userCredentialsRepo userRepo;

    @Autowired
    MongoTemplate mongoTemplate;

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public Optional<User> getUserByName(String name) {
        return userRepo.findByUsername(name);
    }

    public Optional<User> getUserById(String userId) { return userRepo.findById(userId); }

    public User addUser(User user) {
        user.setRole("USER");
        user.setCreatedAt(Date.from(Instant.now()));
        user.setUpdatedAt(user.getCreatedAt());
        return userRepo.save(user);
    }

    public User updateUser(String id, User userDetails) {
        User user = userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUpdatedAt(Date.from(Instant.now()));
        user.setUsername(userDetails.getUsername());
        user.setPassword(userDetails.getPassword());
        user.setEmail(userDetails.getEmail());
        user.setDob(userDetails.getDob());
        user.setBio(userDetails.getBio());
        user.setGender(userDetails.getGender());
        return userRepo.save(user);
    }


    public boolean deleteUsers(String id) {
        Query followingQuery = new Query(Criteria.where("following").is(id));
        List<follows> followingRecords = mongoTemplate.find(followingQuery, follows.class);
        followingRecords.forEach(rec -> userRepo.findById(rec.getFollowed()).ifPresent(user -> {
            user.setFollowers(user.getFollowers() - 1);
            userRepo.save(user);
        }));

        Query followedQuery = new Query(Criteria.where("followed").is(id));
        List<follows> followedRecords = mongoTemplate.find(followedQuery, follows.class);
        followedRecords.forEach(rec -> userRepo.findById(rec.getFollowing()).ifPresent(user -> {
            user.setFollowings(user.getFollowings() - 1);
            userRepo.save(user);
        }));

        Query deleteFollowsQuery = new Query(new Criteria().orOperator(
                Criteria.where("following").is(id),
                Criteria.where("followed").is(id)
        ));
        mongoTemplate.findAllAndRemove(deleteFollowsQuery, follows.class);

        userRepo.findById(id).ifPresent(userRepo::delete);

        return userRepo.findById(id).isEmpty();
    }


    public User getProfile(Authentication authentication, String msgException) throws Exception {
        String name = authentication.getName();

        Optional<User> user = getUserByName(name);
        Query query = new Query();

        if (user.isEmpty())
            throw new Exception(msgException);

        return user.get();
    }

    public List<profileCard> getProfileCard(String username) throws Exception {
        Query query = new Query();
        Query followQuery = new Query();


        query.addCriteria(Criteria.where("username").regex(username, "i"));
        List<User> users = mongoTemplate.find(query, User.class);
        List<profileCard> profiles = new ArrayList<>();
        if (users.isEmpty())
            throw new Exception("No user found with this username");

        for (User user : users) {
            profileCard profile = new profileCard();

            profile = convertToProfileCard(user);

            profiles.add(profile);
        }
        return profiles;
    }

    public profileCard convertToProfileCard(User user){
        profileCard profile = new profileCard();

        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setImageUrl(user.getImageUrl());
        profile.setDob(user.getDob());
        profile.setBio(user.getBio());
        profile.setFollowers(user.getFollowers());
        profile.setFollowings(user.getFollowings());
        profile.setCreatedAt(user.getCreatedAt());
        profile.setUpdatedAt(user.getUpdatedAt());
        profile.setFollow(false);

        return profile;
    }
}
