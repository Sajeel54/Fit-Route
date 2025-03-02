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

@Configuration
public class firebaseConfig {
//
//    @Value("${firebase.credentials.type}")
//    private String type;
//    @Value("${firebase.credentials.project_id}")
//    private String project_id;
//    @Value("${firebase.credentials.private_key_id}")
//    private String private_key_id;
//    @Value("${firebase.credentials.private_key}")
//    private String private_key;
//    @Value("${firebase.credentials.client_email}")
//    private String client_email;
//    @Value("${firebase.credentials.client_id}")
//    private String client_id;
//    @Value("${firebase.credentials.auth_uri}")
//    private String auth_uri;
//    @Value("${firebase.credentials.token_uri}")
//    private String token_uri;
//    @Value("${firebase.credentials.auth_provider_x509_cert_url}")
//    private String auth_provider_x509_cert_url;
//    @Value("${firebase.credentials.client_x509_cert_url}")
//    private String client_x509_cert_url;
//    @Value("${firebase.credentials.universe_domain}")
//    private String universe_domain;

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        String serviceAccountPath = System.getProperty("user.dir") + "/fitroute-firebase-secret.json";
//        JSONObject credentials = new JSONObject();
//        credentials.put("type", type);
//        credentials.put("project_id", project_id);
//        credentials.put("private_key_id", private_key_id);
//        credentials.put("private_key", private_key);
//        credentials.put("client_email", client_email);
//        credentials.put("client_id", client_id);
//        credentials.put("auth_uri", auth_uri);
//        credentials.put("token_uri", token_uri);
//        credentials.put("auth_provider_x509_cert_url", auth_provider_x509_cert_url);
//        credentials.put("client_x509_cert_url", client_x509_cert_url);
//        credentials.put("universe_domain", universe_domain);
//
//        ByteArrayInputStream credentialStream = new ByteArrayInputStream(credentials.toString().getBytes());

        FileInputStream serviceAccountStream = new FileInputStream(serviceAccountPath);

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                .build();
        return FirebaseApp.initializeApp(options);
    }
}