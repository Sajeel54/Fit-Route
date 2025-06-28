package com.fyp.fitRoute.notifications.Repositories;

import com.fyp.fitRoute.notifications.Entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface notificationRepo extends MongoRepository<Notification, String> {
    public List<Notification> findByTo(String to);
}
