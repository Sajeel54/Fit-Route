package com.fyp.fitRoute.posts.Repositories;

import com.fyp.fitRoute.posts.Entity.route;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

@Component
public interface routeRepo extends MongoRepository<route, String> {
}
