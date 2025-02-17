package com.fyp.fitRoute.security.Controllers;

import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.security.Entity.User;
import com.fyp.fitRoute.security.Services.MyUserDetailService;
import com.fyp.fitRoute.security.Utilities.JwtUtils;
import com.fyp.fitRoute.security.Utilities.loginRequest;
import com.fyp.fitRoute.security.Utilities.loginResponse;
import com.fyp.fitRoute.security.Utilities.signupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @PostMapping("/signup")
    @Operation( summary = "Signup and create your account" )
    public ResponseEntity<?> createUser(@RequestBody signupRequest request) {
        try{
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setCreatedAt(Date.from(Instant.now()));
            user.setUpdatedAt(user.getCreatedAt());
            user.setRole("USER");
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
                return new ResponseEntity<>(
                        new loginResponse(),
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
