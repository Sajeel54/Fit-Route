package com.fyp.fitRoute.security.Services;

import com.fyp.fitRoute.security.Entity.User;
import com.fyp.fitRoute.security.Repositories.userCredentialsRepo;
import com.fyp.fitRoute.security.Utilities.JwtUtils;
import com.fyp.fitRoute.security.Utilities.loginResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

@Service
public class googleAuthService {
    @Autowired
    private userCredentialsRepo userRepository;
    private GoogleIdTokenVerifier verifier;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtil;

    private String googleClientId = "333130402881-2l7ihaskg7ds9ogij60pmht5cs4j47sb.apps.googleusercontent.com";

    public googleAuthService() {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    public loginResponse authenticateWithGoogle(String idToken) throws Exception {
        GoogleIdToken googleIdToken = verifier.verify(idToken);
        if (googleIdToken == null) {
            throw new IllegalArgumentException("Invalid ID token");
        }

        GoogleIdToken.Payload payload = googleIdToken.getPayload();
        String googleId = payload.getSubject();
        String email = payload.getEmail();

        // Check if user exists (login) or create new (signup)
        User user = userRepository.findByGoogleId(googleId);
        boolean isNewUser = (user == null);

        if (isNewUser) {
            // Check for email conflict
            if (userRepository.findByEmail(email) != null) {
                throw new IllegalArgumentException("Email already exists");
            }

            user = new User();
            user.setGoogleId(googleId);
            user.setEmail(email);

            // Generate unique username
            String username = email.split("@")[0].replace(".", "_").toLowerCase();
            while (userRepository.findByUsername(username).isPresent()) {
                username += "_" + new Random().nextInt(1000);
            }
            user.setUsername(username);

            // Map Google data
            user.setFirstName((String) payload.get("given_name"));
            user.setLastName((String) payload.get("family_name"));
            user.setImage((String) payload.get("picture"));
            user.setRole("USER"); // 1 = USER
            user.setBio("");
            user.setPassword(passwordEncoder.encode("googleOauth2.0"));

            user.setGender("Unknown");

            user.setDob(null); // Requires birthday scope
            user.setFollowers(0);
            user.setFollowings(0);
            user.setCreatedAt(Date.from(Instant.now()));
            user.setUpdatedAt(user.getCreatedAt());

            userRepository.save(user);
        }

        // Generate JWT
        String jwt = jwtUtil.generateToken(
                loadUserByUsername(user.getUsername())
        );
        return new loginResponse(jwt, jwtUtil.getRole(jwt));
    }

    private String[] getRoles(User user) {
        if (user.getRole() == null) {
            return new String[]{"USER"};
        }
        return user.getRole().split(",");
    }


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isPresent()) {
            var userObj = user.get();
            return org.springframework.security.core.userdetails.User.builder()
                    .username(userObj.getUsername())
                    .password("oauth2.0")
                    .roles(getRoles(userObj))
                    .build();
        } else {
            throw new UsernameNotFoundException(username);
        }
    }
}