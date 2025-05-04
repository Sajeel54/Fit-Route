package com.fyp.fitRoute.security.Utilities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class otpObject {
    private Date expiredAt;
    private String otp;
}
