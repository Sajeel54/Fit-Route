package com.fyp.fitRoute.security.Controllers;

import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.security.Entity.UserCredentials;
import com.fyp.fitRoute.security.Services.MyUserDetailService;
import com.fyp.fitRoute.security.Utilities.JwtUtils;
import com.fyp.fitRoute.security.Utilities.loginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;

@RestController
@RequestMapping("/public")
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

    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@RequestBody UserCredentials user) {
        try{
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            UserCredentials createdUser = uService.addUser(user);
            user.setCreatedAt(Date.from(Instant.now()));
            user.setUpdatedAt(user.getCreatedAt());
            user.setRole("USER");
            user.setImageUrl("");
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e){
            return new ResponseEntity<>("User Already Exists", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/login")
    public ResponseEntity<?> authenticateAndGetToken(@RequestBody loginRequest loginForm) {
        try{
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginForm.getUsername(), loginForm.getPassword()
            ));
            if (authentication.isAuthenticated()) {
                return new ResponseEntity<>(
                        jwtService.generateToken(myUserDetailService.loadUserByUsername(loginForm.getUsername())),
                        HttpStatus.OK
                );
            } else {
                throw new UsernameNotFoundException("Invalid credentials");
            }
        } catch (Exception e){
            return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }
    }

}
