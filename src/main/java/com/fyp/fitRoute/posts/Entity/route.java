package com.fyp.fitRoute.posts.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "route")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class route {
    @Id
    private String id;
    private double distance; // Distance covered in the fitness activity
    private double time; // Time taken to cover the distance (e.g., in minutes or hours)
    private List<coordinates> coordinates;
    private Date createdAt; // Timestamp when the post was created
    private Date updatedAt; // Timestamp when the post was last updated
}
