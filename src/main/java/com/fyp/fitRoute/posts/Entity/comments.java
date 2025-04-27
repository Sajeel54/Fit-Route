package com.fyp.fitRoute.posts.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "comments")
@Data
@AllArgsConstructor
public class comments {
    @Id
    private String id;
    private String referenceId; // ID of another comment if this is a reply
    private String accountId;
    private String postId;
    private String body;
    private int likes;
    private Date createdAt;
    private Date updatedAt;
}
