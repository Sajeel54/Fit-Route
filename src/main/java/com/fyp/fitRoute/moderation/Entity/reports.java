package com.fyp.fitRoute.moderation.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "reports")
@Data
public class reports {
    @Id
    private String id;
    private String reportedUserId;
    private String reportedBy;
    private String reason;
}
