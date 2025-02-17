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
    private String firstName;
    private String lastName;
    private String image;
    private Date dob;
    private String bio;
    private int followers;
    private int followings;
    private boolean follow;
    private Date createdAt;
    private Date updatedAt;

    public profileCard(String username, boolean follow){
        this.id = "";
        this.username = username;
        this.image = "";
        this.dob = null;
        this.bio = "";
        this.followers = 0;
        this.followings = 0;
        this.follow = follow;
        this.createdAt = null;
        this.updatedAt = null;
    }
}
