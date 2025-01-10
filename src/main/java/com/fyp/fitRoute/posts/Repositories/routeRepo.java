package com.fyp.fitRoute.posts.Repositories;

import com.fyp.fitRoute.posts.Entity.route;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface routeRepo extends MongoRepository<route, String> {
}
