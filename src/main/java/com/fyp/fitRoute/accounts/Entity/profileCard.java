package com.fyp.fitRoute.accounts.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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
