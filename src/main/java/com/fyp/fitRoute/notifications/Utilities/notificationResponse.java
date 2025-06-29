package com.fyp.fitRoute.notifications.Utilities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document( collection = "notifications" )
@Data
public class notificationResponse {
    @Id
    private String id;
    private String from;
    private String senderImage;
    private String to;
    private String title;
    private String body;
    private String reference;
    private Date createdAt;
}
