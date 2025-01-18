package com.fyp.fitRoute.posts.Repositories;

import com.fyp.fitRoute.posts.Entity.posts;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface postRepo extends MongoRepository<posts, String> {
    List<posts> findByAccountId(String accountId);
}
