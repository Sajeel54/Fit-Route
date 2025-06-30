package com.fyp.fitRoute.notifications.Repositories;

import com.fyp.fitRoute.notifications.Entity.userConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public interface userConfigRepo extends MongoRepository<userConfig, String> {
    public Optional<userConfig> findByUsername(String username);
    public List<userConfig> findAllBySuspended(boolean suspended);
}
