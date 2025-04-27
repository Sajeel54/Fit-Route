package com.fyp.fitRoute.posts.Utilities;

import lombok.Data;

@Data
public class commentRequest {
    private String referenceId; // ID of another comment if this is a reply
    private String postId;
    private String body;
}
