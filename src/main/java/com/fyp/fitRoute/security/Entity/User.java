package com.fyp.fitRoute.security.Entity;

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
    @NotNull
    private String password;
    private String imageUrl;
    private String role;
    private String email;
    private Date dob;
    private String bio;
    private String gender;
    private int followers;
    private int followings;
    private Date createdAt;
    private Date updatedAt;

}
