package com.fyp.fitRoute.posts.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "likes")
@Data
@AllArgsConstructor
public class likes {
    @Id
    private String id;
    private String accountId;
    private String referenceId; // ID of the post or comment being liked
    private Date createdAt;
    private Date updatedAt;
}
