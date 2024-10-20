package com.fyp.fitRoute.accounts.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class firebaseService {

    @Value("${firebase-base-url}")
    private String imageBaseUrl;

    public String upload(MultipartFile imageFile, String imageName) throws IOException, InterruptedException {
        InputStream inputStream = imageFile.getInputStream();
        Bucket bucket = StorageClient.getInstance().bucket();
        bucket.create(imageName, inputStream, "image/jpeg");
        String url = imageBaseUrl + imageName;
        return getImageUrl(url);
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

}