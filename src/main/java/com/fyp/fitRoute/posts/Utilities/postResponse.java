package com.fyp.fitRoute.posts.Utilities;

import com.fyp.fitRoute.posts.Entity.route;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class postResponse {
    private String id;
    private String title; // Title of the post
    private int likes; // Number of likes the post has received
    private int comments; // Number of comments on the post
    private String username; // User account/ID that created the post
    private String profilePic;
    private String description; // Detailed description of the post
    private List<String> tags; // Tags associated with the post for categorization
    private List<String> images; //Urls of images uploaded bby user
    private String category;
    private Date createdAt; // Timestamp when the post was created
    private Date updatedAt; // Timestamp when the post was last updated
    private boolean like;
    private route route;
}
