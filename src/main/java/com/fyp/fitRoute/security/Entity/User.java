package com.fyp.fitRoute.security.Entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "users")
@Data
@NoArgsConstructor
public class User {
    @Id
    private String id;
    @NotNull
    @Indexed(unique = true)
    private String username;
    private String password;
    private String googleId;
    private String firstName;
    private String lastName;
    private String image;
    private String role;
    @NotNull
    @Email
    private String email;
    private Date dob;
    private String bio;
    private String gender;
    private int followers;
    private int followings;
    private int activities;
    private Date createdAt;
    private Date updatedAt;
    private String modelUrl;
    private Boolean suspended;
}
