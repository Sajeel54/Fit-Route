package com.fyp.fitRoute.posts.Repositories;

import com.fyp.fitRoute.posts.Entity.likes;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public interface likeRepo extends MongoRepository<likes, String> {
    List<likes> findByReferenceId(String referenceId);
    Optional<likes> findByReferenceIdAndAccountId(String referenceId, String accountId);
}
