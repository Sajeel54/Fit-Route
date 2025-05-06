package com.fyp.fitRoute.inventory.Services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
public class cloudinaryService {
    @Autowired
    private Cloudinary cloudinary;

    private static final String CLOUD_NAME = "dwskkawrw";
    private static final String API_BASE_URL = "https://res.cloudinary.com";


    public String uploadImage(String image, String publicId, boolean overwrite) throws IOException {
        if (publicId.isEmpty() || publicId == null)
            throw new IllegalArgumentException("public id cannot be empty");
        try {
            String base64Data = image;
            if (image.contains(",")) {
                base64Data = image.split(",")[1];
            }

            // Decode Base64 to bytes
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            Map uploadResult = cloudinary.uploader().upload(imageBytes,
                    ObjectUtils.asMap(
                            "resource_type", "auto",
                            "public_id", publicId,
                            "overwrite", overwrite
                    ));

            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new IOException("Unable to upload image: " + e.getMessage());
        }
    }

    public void deleteImage(String publicId) throws IOException{
        if (publicId.isEmpty() || publicId == null)
            throw new IllegalArgumentException("public id cannot be empty");
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                    "resource_type", "auto"
            ));
        } catch (Exception e) {
            throw new IOException("Unable to delete image: " + e.getMessage());
        }
    }

    public String uploadModel(File modelFile, String myId) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(modelFile, ObjectUtils.asMap(
                "resource_type", "raw",
                "public_id", myId,
                "overwrite", true));
        return (String) uploadResult.get("url"); // Return the URL of the uploaded file
    }

    public File downloadFile(String fileUrl, String fileName) throws Exception {
        // Open connection
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Check response code
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new Exception("Failed to download file: " + connection.getResponseMessage() + "-" + connection.getResponseCode());
        }

        // Save the file locally
        File tempFile = new File(fileName);
        try (InputStream inputStream = connection.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return tempFile;
    }
}
