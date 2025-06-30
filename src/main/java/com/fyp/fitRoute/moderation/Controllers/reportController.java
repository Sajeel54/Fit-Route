package com.fyp.fitRoute.moderation.Controllers;

import com.fyp.fitRoute.accounts.Services.userService;
import com.fyp.fitRoute.inventory.Utilities.Response;
import com.fyp.fitRoute.moderation.Services.reportService;
import com.fyp.fitRoute.moderation.Utilities.reportRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;

@RestController
@RequestMapping("/report")
@Tag(name = "Report Controller")
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class reportController {
    @Autowired
    private reportService reportService;
    @Autowired
    private userService userServic;

    //add a report
    @PostMapping
    @Operation(summary = "Add a report")
    public ResponseEntity<?> addReport(@RequestBody reportRequest request) {
       try {
           Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
           String reporterId = userServic.getProfile(authentication, "Profile not found").getId();

           String reportedUserId = userServic.getUserByName(request.getReportedUsername()).get().getId();

           // Check if both are same
              if (reporterId.equals(reportedUserId)) {
                return new ResponseEntity<>(new Response("You cannot report yourself", Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
              }


           reportService.saveReport(request.getReason(), reportedUserId ,reporterId, request.getReportedUsername());

           return new ResponseEntity<>(new Response("Report added successfully", Date.from(Instant.now())), HttpStatus.OK);

       } catch (Exception e) {
           log.error("Error adding report: {}", e.getMessage());
           return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
       }

    }

    @GetMapping
    @Operation(summary = "Get all reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllReports() {
        try {
            return new ResponseEntity<>(reportService.getAllReportCards(), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching reports: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/user")
    @Operation(summary = "Get reports by username")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getReportsByUsername(String username) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String reporterId = userServic.getProfile(authentication, "Profile not found").getId();

            return new ResponseEntity<>(reportService.getAllReportsOfUser(reporterId), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching reports by username: {}", e.getMessage());
            return new ResponseEntity<>(new Response(e.getMessage(), Date.from(Instant.now())), HttpStatus.BAD_REQUEST);
        }
    }
}
