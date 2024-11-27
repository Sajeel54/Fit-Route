package com.fyp.fitRoute.accounts.Services;

import com.fyp.fitRoute.accounts.Entity.follows;
import com.fyp.fitRoute.accounts.Entity.profileCard;
import com.fyp.fitRoute.accounts.Repositories.followsRepo;
import com.fyp.fitRoute.security.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class followsService{
    @Autowired
    private followsRepo flwRepo;

    @Autowired
    private MongoTemplate mongoTemplate;

    public follows addFollow(follows entry){
        return flwRepo.save(entry);
    }

    public boolean deleteFollow(String searchId, String myUserId) throws Exception {
        Query query = new Query();
        query.addCriteria(Criteria.where("following").is(myUserId));
        query.addCriteria(Criteria.where("followed").is(searchId));
        follows found = mongoTemplate.findOne(query, follows.class);

        if (found == null)
            throw new Exception("You do not follow this user");

        flwRepo.delete(found);
        found = mongoTemplate.findOne(query, follows.class);

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
        List<profileCard> followers = mongoTemplate.find(query, profileCard.class);
        followers.forEach(follower ->
                follower.setFollow(checkFollow(follower.getId(), userId)));
        return followers;
    }

    public List<profileCard> getFollowing(String userId){
        Query query = new Query();
        query.addCriteria(Criteria.where("following").is(userId));
        List<String> followingIds = mongoTemplate.find(query, follows.class)
                .stream()
                .map(follows::getFollowed)
                .toList();
        query = new Query(Criteria.where("id").in(followingIds));
        List<profileCard> followings = mongoTemplate.find(query, profileCard.class);
        followings.forEach(follower ->
                follower.setFollow(checkFollow(follower.getId(), userId)));
        return followings;
    }
}
