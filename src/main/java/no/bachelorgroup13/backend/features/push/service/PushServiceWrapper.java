package no.bachelorgroup13.backend.features.push.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.messaging.Notification;
import no.bachelorgroup13.backend.features.push.entity.PushNotifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PushServiceWrapper {
    private static final Logger logger = LoggerFactory.getLogger(PushServiceWrapper.class);

    public void sendPush(PushNotifications sub, String title, String body) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Map<String, String> webpushData = new HashMap<>();
            webpushData.put("endpoint", sub.getEndpoint());
            webpushData.put("keys.p256dh", sub.getP256dh());
            webpushData.put("keys.auth", sub.getAuth());


            Message message = Message.builder()
                    .setNotification(notification)
                    .putAllData(webpushData)
                    .setToken(sub.getEndpoint())
                    .build();

            logger.info("Sending push → endpoint: {}  title: {}", sub.getEndpoint(), title);
            String response = FirebaseMessaging.getInstance().send(message);
            logger.info("Successfully sent message: {}", response);

        } catch (FirebaseMessagingException e) {
            logger.error("Failed to send push message via FCM", e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while sending push message", e);
        }
    }
}
