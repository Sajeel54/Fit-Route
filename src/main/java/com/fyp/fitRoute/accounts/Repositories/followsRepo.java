package com.fyp.fitRoute.accounts.Repositories;

import com.fyp.fitRoute.accounts.Entity.follows;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

@Component
public interface followsRepo extends MongoRepository<follows, String> {
}
