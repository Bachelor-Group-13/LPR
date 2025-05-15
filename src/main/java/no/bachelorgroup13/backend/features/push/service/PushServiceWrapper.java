package no.bachelorgroup13.backend.features.push.service;

import com.google.firebase.messaging.*;
import no.bachelorgroup13.backend.features.push.entity.PushNotifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PushServiceWrapper {
    private static final Logger logger = LoggerFactory.getLogger(PushServiceWrapper.class);

    public void sendPush(PushNotifications sub, String title, String body) {
        logger.info("PushServiceWrapper received subscription object:");
        logger.info("  Endpoint: {}", sub != null ? sub.getEndpoint() : "null");
        logger.info("  P256dh: {}", sub != null ? sub.getP256dh() : "null");
        logger.info("  Auth: {}", sub != null ? sub.getAuth() : "null");

        if (sub == null || sub.getEndpoint() == null || sub.getP256dh() == null || sub.getAuth() == null) {
            logger.warn("Attempted to send push to an incomplete subscription: {}", sub);
            return;
        }

        try {
            WebpushNotification notification = WebpushNotification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            WebpushConfig webpushConfig = WebpushConfig.builder()
                    .setNotification(notification)
                    .setFcmOptions(WebpushFcmOptions.builder().setLink("https://129.241.152.242.nip.io/").build())
                    .build();

            String endpoint = sub.getEndpoint();
            String fcmToken = endpoint.substring(endpoint.lastIndexOf("/") + 1);

            logger.info("Extracted FCM token: {}", fcmToken);

            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setWebpushConfig(webpushConfig)
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
