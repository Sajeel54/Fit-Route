package com.fyp.fitRoute.moderation.Repositories;

import com.fyp.fitRoute.moderation.Entity.reports;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public interface reportRepo extends MongoRepository<reports, String> {
    // get by user id
    public List<reports> findByReportedUserId(String reportedUserId);


    // delete by user id
    public void deleteByReportedUserId(String reportedUserId);
}
