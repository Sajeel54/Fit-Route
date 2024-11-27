package com.fyp.fitRoute.accounts.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "users")
@Data
@NoArgsConstructor
public class profileCard {
    private String id;
    private String username;
    private String imageUrl;
    private Date dob;
    private String bio;
    private int followers;
    private int followings;
    private boolean follow;
    private Date createdAt;
    private Date updatedAt;
}
