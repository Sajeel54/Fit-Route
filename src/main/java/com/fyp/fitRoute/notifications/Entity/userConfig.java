package com.fyp.fitRoute.notifications.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document( collection = "userConfig" )
@Data
public class userConfig {
    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    private List<String> notificationsTokens;
    private boolean suspended;
    private Date suspensionEndDate;
}
