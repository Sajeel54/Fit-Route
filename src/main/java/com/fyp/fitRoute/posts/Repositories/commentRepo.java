package com.fyp.fitRoute.posts.Repositories;

import com.fyp.fitRoute.posts.Entity.comments;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface commentRepo extends MongoRepository<comments, String> {
    List<comments> findByPostId(String postId);
    List<comments> findByReferenceId(String referenceId);
}
