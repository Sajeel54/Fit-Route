package com.fyp.fitRoute.posts.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "posts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class posts {
    @Id
    private String id;
    private String title; // Title of the post
    private int likes; // Number of likes the post has received
    private int comments; // Number of comments on the post
    private String routeId; // ID of the route associated with the post
    private String accountId; // User account/ID that created the post
    private String description; // Detailed description of the post
    private List<String> tags; // Tags associated with the post for categorization
    private List<String> images; //Urls of images uploaded bby user
    private String category;
    private Date createdAt; // Timestamp when the post was created
    private Date updatedAt; // Timestamp when the post was last updated
}
