package com.fyp.fitRoute.moderation.Repositories;

import com.fyp.fitRoute.notifications.Entity.userConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface userConfigRepo extends MongoRepository<userConfig, String> {
    userConfig findByUsername(String username);
    userConfig findByUserId(String userId);
    void deleteByUsername(String username);
    void deleteByUserId(String userId);
    public List<userConfig> findAllBySuspended(boolean suspended);
}
