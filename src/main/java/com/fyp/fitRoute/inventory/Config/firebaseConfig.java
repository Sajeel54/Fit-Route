package com.fyp.fitRoute.inventory.Config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class firebaseConfig {
    @Value("{storage-bucket}")
    private String bucket;
    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        String serviceAccountPath = System.getProperty("user.dir") + "/fitroute-firebase-secret.json";
        FileInputStream serviceAccountStream = new FileInputStream(serviceAccountPath);

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                .setStorageBucket(bucket)
                .build();
        return FirebaseApp.initializeApp(options);
    }
}