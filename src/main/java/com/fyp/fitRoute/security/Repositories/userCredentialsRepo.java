package com.fyp.fitRoute.security.Repositories;

import com.fyp.fitRoute.security.Entity.UserCredentials;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface userCredentialsRepo extends MongoRepository<UserCredentials, String> {
    Optional<UserCredentials> findByUsername(String username);
}
