package com.fyp.fitRoute.inventory.Config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class cloudinaryConfig {

    @Value("${Cloudinary.cloud_name}")
    private String cloudName;
    @Value("${Cloudinary.api_key}")
    private String apiKey;
    @Value("${Cloudinary.api_secret}")
    private String apiSecret;

    @Bean
    public Cloudinary initializeCloudinary(){
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }
}
