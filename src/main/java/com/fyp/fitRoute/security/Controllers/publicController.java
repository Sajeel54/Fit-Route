package com.fyp.fitRoute.security.Controllers;

import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.recommendations.Components.ANN.annModel;
import com.fyp.fitRoute.security.Entity.User;
import com.fyp.fitRoute.security.Services.MyUserDetailService;
import com.fyp.fitRoute.security.Services.googleAuthService;
import com.fyp.fitRoute.security.Services.otpService;
import com.fyp.fitRoute.security.Utilities.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;

@RestController
@RequestMapping("/public")
@Tag( name="Public Controller" , description = "These endpoints are public and require no Authentication")
public class publicController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtService;
    @Autowired
    private MyUserDetailService myUserDetailService;
    @Autowired
    private userService uService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private otpService otpService;
    @Autowired
    private googleAuthService oauthService;
    @Autowired
    private annModel model;

    @GetMapping("/forgot-Password")
    public ResponseEntity<?> forgotPassword(@RequestParam String username){
        try {
            User user = uService.getUser(username);
            String email = otpService.send(user);
            return new ResponseEntity<>(new emailObject(email), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String username, @RequestParam String otp){
        try {
            UserDetails user = null;
            if (otpService.verifyOtp(username, otp))
                user = myUserDetailService.loadUserByUsername(username);

            if (user==null)
                throw new RuntimeException("User not found");

            String token = jwtService.generateToken(user);

            loginResponse response = new loginResponse(token);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/Oauth2/google")
    @Operation( summary = "Signup and create your account" )
    public ResponseEntity<?> UserOAuth(@RequestParam String token) {
        try{
            loginResponse response = oauthService.authenticateWithGoogle(token);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/signup")
    @Operation( summary = "Signup and create your account" )
    public ResponseEntity<?> createUser(@RequestBody signupRequest request) {
        try{
            if (request.getPassword() == null && request.getPassword().isEmpty())
                throw new RuntimeException("Password cannot be empty");
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setCreatedAt(Date.from(Instant.now()));
            user.setUpdatedAt(user.getCreatedAt());
            user.setRole("USER");
            user.setActivities(0);
            user.setBio("");
            user.setFollowers(0);
            user.setFollowings(0);
            user.setGoogleId(null);
            user.setEmail(request.getEmail());
            User createdUser = uService.addUser(user);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e){
            return new ResponseEntity<>("User Already Exists", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    @Operation( summary = "Login your account" )
    public ResponseEntity<?> authenticateAndGetToken(@RequestBody loginRequest loginForm) {
        try{
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginForm.getUsername(), loginForm.getPassword()
            ));
            if (authentication.isAuthenticated()) {
                String generatedToken = jwtService.generateToken(myUserDetailService.loadUserByUsername(loginForm.getUsername()));
                loginResponse login = new loginResponse(generatedToken);
                return new ResponseEntity<>(
                        login,
                        HttpStatus.OK
                );
            } else {
                throw new UsernameNotFoundException("Invalid credentials");
            }
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

}
