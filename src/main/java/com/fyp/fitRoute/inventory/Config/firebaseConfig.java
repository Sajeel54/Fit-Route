package com.fyp.fitRoute.inventory.Config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class firebaseConfig {

    @Value("${firebase_credentials}")
    private String firebase_credentials;

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {

        ByteArrayInputStream credentialStream = new ByteArrayInputStream(firebase_credentials.getBytes(StandardCharsets.UTF_8));

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(credentialStream))
                .build();
        return FirebaseApp.initializeApp(options);
    }
}