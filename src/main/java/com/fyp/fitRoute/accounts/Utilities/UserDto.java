package com.fyp.fitRoute.accounts.Utilities;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;

@Data
public class UserDto {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String image;
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
