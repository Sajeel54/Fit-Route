package com.fyp.fitRoute.accounts.Services;

import com.fyp.fitRoute.accounts.Entity.follows;
import com.fyp.fitRoute.accounts.Entity.profileCard;
import com.fyp.fitRoute.accounts.Utilities.profileRequest;
import com.fyp.fitRoute.inventory.Services.cloudinaryService;
import com.fyp.fitRoute.posts.Entity.comments;
import com.fyp.fitRoute.posts.Entity.likes;
import com.fyp.fitRoute.posts.Entity.posts;
import com.fyp.fitRoute.posts.Entity.route;
import com.fyp.fitRoute.security.Entity.User;
import com.fyp.fitRoute.security.Repositories.userCredentialsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class userService {
    @Autowired
    private userCredentialsRepo userRepo;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    private cloudinaryService cloudinaryService;

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

    @Transactional
    public User updateUser(String id, User userDetails) throws IOException {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUpdatedAt(Date.from(Instant.now()));

        user.setUsername(Optional.ofNullable(userDetails.getUsername())
                .filter(username -> !username.isEmpty()).orElse(user.getUsername()));

        user.setFirstName(Optional.ofNullable(userDetails.getFirstName())
                .filter(name -> !name.isEmpty()).orElse(user.getFirstName()));

        user.setLastName(Optional.ofNullable(userDetails.getLastName())
                .filter(name -> !name.isEmpty()).orElse(user.getLastName()));

        user.setPassword(Optional.ofNullable(userDetails.getPassword())
                .filter(password -> !password.isEmpty()).orElse(user.getPassword()));

        user.setEmail(Optional.ofNullable(userDetails.getEmail())
                .filter(email -> !email.isEmpty()).orElse(user.getEmail()));

        user.setDob(Optional.ofNullable(userDetails.getDob())
                .orElse(user.getDob()));

        user.setBio(Optional.ofNullable(userDetails.getBio())
                .filter(bio -> !bio.isEmpty()).orElse(user.getBio()));

        user.setGender(Optional.ofNullable(userDetails.getGender())
                .filter(gender -> !gender.isEmpty()).orElse(user.getGender()));

        if (userDetails.getImage() != null && !(userDetails.getImage().isEmpty())) {
                String url = cloudinaryService.uploadImage(userDetails.getImage(), user.getId(), true);
                userDetails.setImage(url);
            }


        return userRepo.save(user);
    }

    @Transactional
    public User setUpAccount(profileRequest userDetails, User user) throws IOException {
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setDob(userDetails.getDob());
        user.setBio(userDetails.getBio());
        user.setGender(userDetails.getGender());
        String url = cloudinaryService.uploadImage(userDetails.getImage(), user.getId(), false);
        user.setImage(url);
        return userRepo.save(user);
    }

    @Transactional
    public boolean deleteUsers(String id) throws IOException {
        Query followingQuery = new Query(Criteria.where("following").is(id));
        List<String> followingRecords = mongoTemplate.find(followingQuery, follows.class)
                .stream()
                .map(follows::getFollowed)
                .toList();
        followingRecords.forEach(rec -> userRepo.findById(rec).ifPresent(user -> {
            user.setFollowers(user.getFollowers() - 1);
            userRepo.save(user);
        }));

        Query followedQuery = new Query(Criteria.where("followed").is(id));
        List<String> followedRecords = mongoTemplate.find(followedQuery, follows.class)
                .stream()
                .map(follows::getFollowed)
                .toList();
        followedRecords.forEach(rec -> userRepo.findById(rec).ifPresent(user -> {
            user.setFollowings(user.getFollowings() - 1);
            userRepo.save(user);
        }));

        Query deleteFollowsQuery = new Query(new Criteria().orOperator(
                Criteria.where("following").is(id),
                Criteria.where("followed").is(id)
        ));

        mongoTemplate.findAllAndRemove(deleteFollowsQuery, follows.class);
        mongoTemplate.findAllAndRemove(new Query(Criteria.where("accountId").is(id)), comments.class);
        mongoTemplate.findAllAndRemove(new Query(Criteria.where("accountId").is(id)), likes.class);

        List<posts> posts = mongoTemplate.findAllAndRemove(new Query(Criteria.where("accountId").is(id)), posts.class);
        List<String> routeIds = posts.stream().map(com.fyp.fitRoute.posts.Entity.posts::getRouteId).toList();
        List<String> postIds = posts.stream().map(com.fyp.fitRoute.posts.Entity.posts::getId).toList();

        mongoTemplate.findAllAndRemove(new Query(Criteria.where("id").in(routeIds)), route.class);
        mongoTemplate.findAllAndRemove(new Query(Criteria.where("postId").in(postIds)), comments.class);
        mongoTemplate.findAllAndRemove(new Query(Criteria.where("postId").in(postIds)), likes.class);
        
        userRepo.findById(id).ifPresent(userRepo::delete);
        cloudinaryService.deleteImage(id);
        return userRepo.findById(id).isEmpty();
    }


    public User getProfile(Authentication authentication, String msgException) throws RuntimeException {
        String name = authentication.getName();

        Optional<User> user = getUserByName(name);

        if (user.isEmpty())
            throw new RuntimeException(msgException);

        user.get().setImage(user.get().getImage()+"?t="+user.get().getUpdatedAt().getTime());
        return user.get();
    }

    public List<profileCard> getProfileCard(String username){
        return mongoTemplate.find(
                new Query(Criteria.where("username").regex(username, "i")),
                profileCard.class);
    }

}
