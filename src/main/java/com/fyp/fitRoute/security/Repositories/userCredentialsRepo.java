package com.fyp.fitRoute.security.Repositories;

import com.fyp.fitRoute.security.Entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface userCredentialsRepo extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    User findByGoogleId(String googleId);
    User findByEmail(String email);
}
