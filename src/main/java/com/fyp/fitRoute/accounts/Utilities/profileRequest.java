package com.fyp.fitRoute.accounts.Utilities;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Date;

@Data
public class profileRequest {
    @NotEmpty
    private String firstName;
    @NotEmpty
    private String lastName;
    @NotEmpty
    private String image;
    private Date dob;
    private String bio;
    private String gender;
}
