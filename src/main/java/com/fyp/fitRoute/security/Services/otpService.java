package com.fyp.fitRoute.security.Services;

import com.fyp.fitRoute.inventory.Services.EmailService;
import com.fyp.fitRoute.inventory.Services.redisService;
import com.fyp.fitRoute.security.Entity.User;
import com.fyp.fitRoute.security.Utilities.otpObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

@Service
public class otpService {
    private Map<String, otpObject> otpList = new HashMap();
    @Autowired
    private redisService rdsService;
    @Autowired
    private EmailService emailService;


    public void ejectExpiredOtp(){
        for (Map.Entry<String, otpObject> entry : otpList.entrySet()) {
            if (entry.getValue().getExpiredAt().getTime() < System.currentTimeMillis()){
                otpList.remove(entry.getKey());
            }
        }
    }
    public String send(User user){
        String otp = (100000 + (new Random()).nextInt(900000))+"";
        ejectExpiredOtp();

        if (otpList.containsValue(otp))
            send(user);

        if (user.getEmail()==null || user.getEmail().isEmpty())
            throw new RuntimeException("Email not set");

        emailService.sendMail(user.getEmail(),
                "Your OTP Code",
                "Your one-time password is: " + otp + "\nValid for 5 minutes.");
        rdsService.set("otp of "+user.getUsername(), otp, 300L);
        otpList.put(user.getUsername(), new otpObject(new java.util.Date(System.currentTimeMillis() + 300000), otp));
        return user.getEmail();
    }

    public boolean verifyOtp(String username, String input){

        String otp = rdsService.get("otp of "+username, String.class);

        if (otp != null){
            if (Objects.equals(otp, input)){
                otpList.remove(username);
                return true;
            }
            else
                throw new RuntimeException("Wrong otp");
        } else {
            throw new RuntimeException("Otp expired");
        }
    }
}
