package com.fyp.fitRoute.posts.Utilities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class likeResponse {
    private String id;
    private String username;
    private String imageUrl;
    private String postId;
    private Date createdAt;
    private Date updatedAt;
}
