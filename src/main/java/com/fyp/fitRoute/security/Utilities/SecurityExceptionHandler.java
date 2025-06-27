package com.fyp.fitRoute.security.Utilities;

import com.fyp.fitRoute.inventory.Utilities.Response;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.Date;

@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleForbiddenPath(NoResourceFoundException ex) {
        return new ResponseEntity<>(new Response("No such API endpoint exists", Date.from(Instant.now())), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        return new ResponseEntity<>("Not Authorized", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> handleTokenExpired(ExpiredJwtException ex) {
        return new ResponseEntity<>(new Response("Token Expired", Date.from(Instant.now())), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<?> handleMalformedToken(SignatureException ex) {
        return new ResponseEntity<>(new Response("Token Malformed", Date.from(Instant.now())), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserSuspendedException.class)
    public ResponseEntity<?> userSuspended(UserSuspendedException ex) {
        return new ResponseEntity<>(new Response(ex.getMessage(), Date.from(Instant.now())), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> runtimeException(RuntimeException ex) {
        return new ResponseEntity<>(new Response(ex.getMessage(), Date.from(Instant.now())), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
