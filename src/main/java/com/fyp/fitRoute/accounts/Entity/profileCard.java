package com.fyp.fitRoute.accounts.Entity;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import lombok.Data;

@Document(collection = "users")
@Data
public class profileCard {
    @Id
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String image;
    private int followers;
    private int activities;
    private boolean follow;
}
