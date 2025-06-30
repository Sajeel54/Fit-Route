package com.fyp.fitRoute.notifications.Services;

import com.fyp.fitRoute.inventory.Services.firebaseService;
import com.fyp.fitRoute.notifications.Entity.Notification;
import com.fyp.fitRoute.notifications.Entity.userConfig;
import com.fyp.fitRoute.notifications.Repositories.notificationRepo;
import com.fyp.fitRoute.notifications.Repositories.userConfigRepo;
import com.fyp.fitRoute.notifications.Utilities.notificationResponse;
import com.fyp.fitRoute.security.Entity.User;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class notificationService {
    @Autowired
    private notificationRepo notificationRepo;
    @Autowired
    private firebaseService frbService;
    @Autowired
    private userConfigRepo userConRepo;
    @Autowired
    private MongoTemplate mongoTemplate;

    public String deliverNotification(
            String title,
            String body,
            @NonNull String to,
            @NonNull String from,
            String ref
    ) throws FirebaseMessagingException {

        if (Objects.equals(to, from))
            return "Cannot send notification to yourself";

        Notification notification = new Notification();
        notification.setTo(to);
        notification.setFrom(from);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setReference(ref);
        List<String> tokens = getTokenByUsername(notification.getTo());
        String response = "";
        try {
            for (String token : tokens) {
                response = frbService.sendNotification(title, body, token);
            }
            log.info("Notification sent to: {}", to);
            notificationRepo.save(notification);
        } catch (Exception ex) {
            response = "Not successful";
        }

        return response;

    }

    public void registerToken(String username, @NonNull String token){
        userConfig userCon = userConRepo.findByUsername(username)
                .orElseGet(() -> {
                    userConfig newUserCon = new userConfig();
                    newUserCon.setUsername(username);
                    newUserCon.setNotificationsTokens(new ArrayList<>());
                    newUserCon.setSuspended(false);
                    newUserCon.setSuspensionEndDate(null);
                    return newUserCon;
                });
        List<String> tokens= userCon.getNotificationsTokens();
        if (tokens.contains(token))
            return;
        tokens.add(token);
        userCon.setNotificationsTokens(tokens);
        userCon.setUsername(username);
        userCon.setSuspended(userCon.isSuspended());
        userCon.setSuspensionEndDate(userCon.getSuspensionEndDate());
        userConRepo.save(userCon);
    }

    public boolean deleteToken(String username, @NonNull String token){
        Optional<userConfig> userConFound = userConRepo.findByUsername(username);
        if (userConFound.isEmpty())
            return false;
        userConfig userCon = userConFound.get();
        List<String> tokens = userCon.getNotificationsTokens();
        if (!tokens.contains(token))
            return false;
        tokens.remove(token);
        userCon.setNotificationsTokens(tokens);
        userCon.setUsername(username);
        userCon.setSuspended(userCon.isSuspended());
        userCon.setSuspensionEndDate(userCon.getSuspensionEndDate());
        userConRepo.save(userCon);
        return true;
    }

    public void refreshToken(String username, @NonNull String oldToken, @NonNull String newToken){
        Optional<userConfig> userConFound = userConRepo.findByUsername(username);
        if (userConFound.isEmpty()){
            registerToken(username, newToken);
            return;
        }
        userConfig userCon = userConFound.get();
        List<String> tokens = userCon.getNotificationsTokens();
        tokens.remove(oldToken);
        tokens.add(newToken);
        userCon.setNotificationsTokens(tokens);
        userCon.setUsername(username);
        userCon.setSuspended(userCon.isSuspended());
        userCon.setSuspensionEndDate(userCon.getSuspensionEndDate());
        userConRepo.save(userCon);
    }

    public List<String> getTokenByUsername(String username){
        Optional<userConfig> userCon = userConRepo.findByUsername(username);
        if (userCon.isEmpty())
            throw new RuntimeException("Register yourself");
        return userCon.get().getNotificationsTokens();
    }

    public List<notificationResponse> getNotifications(String username){
        List<Notification> notifications= notificationRepo.findByTo(username);
        List<notificationResponse> responses = new ArrayList<>();
        for (Notification notification : notifications) {
            notificationResponse response = new notificationResponse();
            response.setId(notification.getId());
            response.setFrom(notification.getFrom());
            response.setTo(notification.getTo());
            response.setTitle(notification.getTitle());
            response.setBody(notification.getBody());
            response.setReference(notification.getReference());
            response.setCreatedAt(notification.getCreatedAt());

            // fetch sender
            User sender = mongoTemplate.findById(notification.getFrom(), User.class);
            if (sender != null) {
                response.setSenderImage(sender.getImage());
            } else {
                response.setSenderImage("default_image_url"); // Set a default image URL if sender not found
            }

            responses.add(response);
        }
        return responses;
    }
}
