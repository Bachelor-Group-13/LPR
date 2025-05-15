package no.bachelorgroup13.backend.features.push.service;

import com.google.firebase.messaging.*;
import no.bachelorgroup13.backend.features.push.entity.PushNotifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PushServiceWrapper {
    private static final Logger logger = LoggerFactory.getLogger(PushServiceWrapper.class);

    public void sendPush(PushNotifications sub, String title, String body) {
        if (sub == null || sub.getEndpoint() == null || sub.getP256dh() == null || sub.getAuth() == null) {
            logger.warn("Attempted to send push to an incomplete subscription: {}", sub);
            return;
        }

        try {
            WebpushNotification webpushNotification = WebpushNotification.builder()
                    .setTitle(title)
                    .setBody(body)
                    // You can add other Web Push specific options here (icon, badge, etc.)
                    // .setIcon("/icons/icon-192x192.png") // Example
                    .build();

            WebpushConfig webpushConfig = WebpushConfig.builder()
                    .setNotification(webpushNotification)
                    .setFcmOptions(WebpushFcmOptions.builder().setLink("https://129.241.152.242.nip.io/").build())
                    .build();

            Message message = Message.builder()
                    .setWebpushConfig(webpushConfig)
                    .setToken(sub.getEndpoint())
                    .build();


            logger.info("Sending push → endpoint: {}  title: {}", sub.getEndpoint(), title);

            String response = FirebaseMessaging.getInstance().send(message);

            logger.info("Successfully sent message: {}", response);

        } catch (FirebaseMessagingException e) {
            logger.error("Failed to send push message via FCM to endpoint: {}", sub.getEndpoint(), e);
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                logger.warn("Subscription expired or invalid, removing from database: {}", sub.getEndpoint());
            }
        } catch (Exception e) {
            logger.error("An unexpected error occurred while sending push message to endpoint: {}", sub.getEndpoint(), e);
        }
    }
}
