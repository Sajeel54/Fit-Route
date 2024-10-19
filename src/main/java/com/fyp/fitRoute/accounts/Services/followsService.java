package com.fyp.fitRoute.accounts.Services;

import com.fyp.fitRoute.accounts.Entity.follows;
import com.fyp.fitRoute.accounts.Repositories.followsRepo;
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

    public List<follows> getFollowers(String userId){
        Query query = new Query();
        query.addCriteria(Criteria.where("followed").is(userId));
        return mongoTemplate.find(query, follows.class);
    }

    public List<follows> getFollowing(String userId){
        Query query = new Query();
        query.addCriteria(Criteria.where("following").is(userId));
        return mongoTemplate.find(query, follows.class);
    }
}
