package com.fyp.fitRoute.progress.Services;

import com.fyp.fitRoute.progress.Entity.progress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class tracker {
    @Autowired
    private MongoTemplate mongoCon;
    //run every week at 00:00 on Monday
    @Scheduled(cron = "0 0 0 * * MON")
    public void renewProgressWeekly() {
        // Reset the daily distance for all users to zero at the start of a new week
        Query query = new Query();
        query.addCriteria(Criteria.where("dailyDistance").exists(true));

        // Use MongoTemplate to update all documents in the progress collection
        mongoCon.updateMulti(query,
            new org.springframework.data.mongodb.core.query.Update().set("dailyDistance", new double[7]),
            "progress");
    }

    public progress getProgressByUserId(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        progress userProgress = mongoCon.findOne(query, progress.class, "progress");
        if (userProgress == null) {
            userProgress = new progress();
            userProgress.setUserId(userId);
            userProgress.setDailyDistance(new double[7]); // Initialize with 7 days of zero distance
            mongoCon.save(userProgress, "progress");
        }
        return userProgress;
    }
}
