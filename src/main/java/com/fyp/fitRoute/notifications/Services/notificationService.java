package com.fyp.fitRoute.notifications.Services;

import com.fyp.fitRoute.inventory.Services.firebaseService;
import com.fyp.fitRoute.notifications.Entity.Notification;
import com.fyp.fitRoute.notifications.Entity.userConfig;
import com.fyp.fitRoute.notifications.Repositories.notificationRepo;
import com.fyp.fitRoute.notifications.Repositories.userConfigRepo;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class notificationService {
    @Autowired
    private notificationRepo notificationRepo;
    @Autowired
    private firebaseService frbService;
    @Autowired
    private userConfigRepo userConRepo;

    public String deliverNotification(
            String title,
            String body,
            @NonNull String to,
            @NonNull String from,
            String ref
    ) throws FirebaseMessagingException {

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
                    newUserCon.setNotificationsTokens(List.of(token));
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

    public List<Notification> getNotifications(String username){
        return notificationRepo.findByTo(username);
    }
}
