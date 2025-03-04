package com.fyp.fitRoute.inventory.Services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
public class cloudinaryService {
    @Autowired
    private Cloudinary cloudinary;

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
}
