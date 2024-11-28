package com.fyp.fitRoute.notifications.Repositories;

import com.fyp.fitRoute.notifications.Entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

@Component
public interface notificationRepo extends MongoRepository<Notification, String> {
}
