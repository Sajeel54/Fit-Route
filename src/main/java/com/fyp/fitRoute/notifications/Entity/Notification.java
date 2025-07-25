package com.fyp.fitRoute.notifications.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document( collection = "notifications" )
@Data
public class Notification {
    @Id
    private String id;
    private String from;
    private String to;
    private String title;
    private String body;
    private String reference;
    private Date createdAt;

}
