package com.fyp.fitRoute.posts.Repositories;

import com.fyp.fitRoute.posts.Entity.posts;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface postRepo extends MongoRepository<posts, String> {
}
