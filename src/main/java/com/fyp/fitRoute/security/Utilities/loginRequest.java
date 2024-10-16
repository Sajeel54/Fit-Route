package com.fyp.fitRoute.security.Utilities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class loginRequest {
    private String username;
    private String password;
}
