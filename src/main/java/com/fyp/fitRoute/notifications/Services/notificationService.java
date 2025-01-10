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
        String token = getTokenByUsername(notification.getTo());
        return frbService.sendNotification(title, body, token);

    }

    public void registerToken(String username, String Token){
        userConfig userCon = new userConfig();
        userCon.setNotificationsToken(Token);
        userCon.setUsername(username);
        userCon.setOtp(0);
        userConRepo.save(userCon);
    }

    public String getTokenByUsername(String username){
        Optional<userConfig> userCon = userConRepo.findByUsername(username);
        if (userCon.isEmpty())
            throw new RuntimeException("Register yourself");
        return userCon.get().getNotificationsToken();
    }
}
