package com.fyp.fitRoute.notifications.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document( collection = "userConfig" )
@Data
public class userConfig {
    @Id
    private String id;
    private String username;
    private String notificationsToken;
    private int otp;
}
