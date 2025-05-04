package com.fyp.fitRoute.security.Utilities;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class loginRequest {
    @NotNull
    private String username;
    @NotNull
    private String password;
}
