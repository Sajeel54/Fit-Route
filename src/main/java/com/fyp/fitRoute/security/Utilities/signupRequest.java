package com.fyp.fitRoute.security.Utilities;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Date;

@Data
public class signupRequest {
    @NotEmpty
    private String username;
    @NotEmpty
    private String password;
    @NotEmpty
    private String email;
    @NotEmpty
    private Date dob;
    @NotEmpty
    private String bio;
    @NotEmpty
    private String gender;
}
