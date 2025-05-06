package com.fyp.fitRoute.recommendations.Services;

import com.fyp.fitRoute.inventory.Services.redisService;
import com.fyp.fitRoute.posts.Utilities.postResponse;
import com.fyp.fitRoute.recommendations.Components.ANN.annModel;
import com.fyp.fitRoute.recommendations.Components.annFilter;
import com.fyp.fitRoute.recommendations.Components.filterManager;
import com.fyp.fitRoute.recommendations.Components.socialFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class recommendationService {
    @Autowired
    private filterManager manager;

    @Autowired
    private redisService redisService;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    private socialFilter socialFilter;

    @Autowired
    private annModel model;

    public void setFilters(String myId){
        manager.clearFilters();
        manager.addFilter(socialFilter);
//        manager.addFilter(annFilter);
        manager.addFilter(model);
    }

    public List<postResponse> getRecommendations(String myId) throws Exception {
        setFilters(myId);
        Date date = Date.from(Instant.now());

        Date accessTimeStamp = Objects.requireNonNullElseGet(
                redisService.get("Recommendations Access " + myId, Date.class),
                () -> Date.from(Instant.now().minusSeconds(48L * 60L * 60L))
        );
        redisService.set("Recommendations Access " + myId, date, -1L);
        manager.setTimeStamp(accessTimeStamp);
        manager.setMyId(myId);
        manager.start();

        return manager.getPosts();
    }
}
