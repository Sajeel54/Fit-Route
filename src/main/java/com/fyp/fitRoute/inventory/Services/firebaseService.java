package com.fyp.fitRoute.inventory.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

@Service
public class firebaseService {
    @Value("${firebase.image-base-url}")
    private String imageBaseUrl;

    public String uploadImage(MultipartFile imageFile, String imageName) throws IOException, InterruptedException {
        InputStream inputStream = imageFile.getInputStream();
        Bucket bucket = StorageClient.getInstance().bucket();
        bucket.create(imageName, inputStream, "image/jpeg");
        String url = imageBaseUrl + imageName;
        return getImageUrl(url);
    }

    public String uploadImage(String imageFile, String imageName) throws IOException, InterruptedException {
        byte[] imageBytes = Base64.getDecoder().decode(imageFile);

        // Convert to InputStream
        InputStream inputStream = new ByteArrayInputStream(imageBytes);

        // Upload to Firebase bucket
        Bucket bucket = StorageClient.getInstance().bucket();
        bucket.create(imageName, inputStream, "image/jpeg");
        String url = imageBaseUrl + imageName;
        return getImageUrl(url);
    }

    public String deleteImage(String imageName){
        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.get(imageName);
        if (blob != null && blob.exists()){
            if (blob.delete())
                return "Image deleted successfully";

            throw new RuntimeException("Failed to delete image");
        }
        throw new RuntimeException("Image not found");
    }

    public String getImageUrl(String baseUrl) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String jsonResponse = response.body();
        JsonNode node = new ObjectMapper().readTree(jsonResponse);
        String imageToken = node.get("downloadTokens").asText();
        return baseUrl + "?alt=media&token=" + imageToken;
    }

    public String sendNotification(String title, String body, String token) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        String response = FirebaseMessaging.getInstance().send(message);
        return "Successfully sent message: " + response;
    }

}