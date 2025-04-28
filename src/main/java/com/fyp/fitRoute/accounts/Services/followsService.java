package com.fyp.fitRoute.accounts.Services;

import com.fyp.fitRoute.accounts.Entity.follows;
import com.fyp.fitRoute.accounts.Entity.profile;
import com.fyp.fitRoute.accounts.Entity.profileCard;
import com.fyp.fitRoute.accounts.Repositories.followsRepo;
import com.fyp.fitRoute.security.Entity.User;
import com.fyp.fitRoute.security.Repositories.userCredentialsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class followsService{
    @Autowired
    private followsRepo flwRepo;
    @Autowired
    private userCredentialsRepo userRepo;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Transactional
    public follows addFollow(User follower, User followed){
        follows entry = new follows();
        Date date = Date.from(Instant.now());

        if (followed.getId() == follower.getId())
            throw new RuntimeException("user unable of being followed");

        entry.setFollowing(follower.getId());
        entry.setFollowed(followed.getId());
        entry.setCreatedAt(date);
        follower.setFollowings(((follower.getFollowings())+1));
        followed.setFollowers(((followed.getFollowers())+1));
        userRepo.save(follower);
        userRepo.save(followed);
        return flwRepo.save(entry);
    }

    @Transactional
    public boolean deleteFollow(User follower, User followed) throws Exception {
        Query query = new Query();
        query.addCriteria(Criteria.where("following").is(follower.getId()));
        query.addCriteria(Criteria.where("followed").is(followed.getId()));
        follows found = mongoTemplate.findOne(query, follows.class);

        if (found == null)
            throw new RuntimeException("You do not follow this user");

        follower.setFollowers(follower.getFollowers()-1);
        followed.setFollowers(followed.getFollowers()-1);
        userRepo.save(follower);
        userRepo.save(followed);
        flwRepo.delete(found);
        found = mongoTemplate.findOne(query, follows.class);
        if (found != null)
            throw new RuntimeException("");

        return found == null;
    }

    public boolean checkFollow(String followerId, String followedId){

        Query query = new Query();
        query.addCriteria(Criteria.where("following").is(followerId));
        query.addCriteria(Criteria.where("followed").is(followedId));
        follows found = mongoTemplate.findOne(query, follows.class);

        return found != null;
    }

    public List<profileCard> getFollowers(String userId){
        Query query = new Query();
        query.addCriteria(Criteria.where("followed").is(userId));
        List<String> followerIds = mongoTemplate.find(query, follows.class)
                .stream()
                .map(follows::getFollowing)
                .toList();
        query = new Query(Criteria.where("id").in(followerIds));
        return mongoTemplate.find(query, profileCard.class);
    }

    public List<profileCard> getFollowing(String userId){
        Query query = new Query();
        query.addCriteria(Criteria.where("following").is(userId));
        List<String> followingIds = mongoTemplate.find(query, follows.class)
                .stream()
                .map(follows::getFollowed)
                .toList();
        query = new Query(Criteria.where("id").in(followingIds));
        return mongoTemplate.find(query, profileCard.class);
    }
}
