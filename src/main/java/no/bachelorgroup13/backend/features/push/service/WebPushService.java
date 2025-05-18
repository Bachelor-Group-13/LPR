package no.bachelorgroup13.backend.features.push.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import nl.martijndwars.webpush.Urgency;
import no.bachelorgroup13.backend.features.push.entity.PushNotifications;
import no.bachelorgroup13.backend.features.push.repository.PushSubscriptionRepository;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for sending web push notifications to subscribed users.
 * Handles notification delivery and subscription management.
 */
@Service
public class WebPushService {
    private static final Logger logger = LoggerFactory.getLogger(WebPushService.class);
    private final PushService pushService;
    private final PushSubscriptionRepository pushRepository;

    /**
     * Constructs a new WebPushService with VAPID keys and subject.
     * @param publicKey VAPID public key
     * @param privateKey VAPID private key
     * @param subject VAPID subject
     * @param pushRepository Repository for managing push subscriptions
     */
    public WebPushService(
            @Value("${vapid.keys.public}") String publicKey,
            @Value("${vapid.keys.private}") String privateKey,
            @Value("${vapid.subject}") String subject,
            PushSubscriptionRepository pushRepository) {
        this.pushRepository = pushRepository;

        Security.addProvider(new BouncyCastleProvider());

        try {
            logger.info("Initializing Web Push Service with:");
            logger.info("Public Key: {}", publicKey);
            logger.info("Private Key: {}", privateKey);
            logger.info("Subject: {}", subject);
            this.pushService = new PushService(publicKey, privateKey);
            this.pushService.setSubject(subject);

            logger.info("Web Push Service initialized successfully with public key: {}", publicKey);
        } catch (GeneralSecurityException e) {
            logger.error("Failed to initialize Web Push Service", e);
            throw new RuntimeException("Failed to initialize Web Push Service", e);
        }
    }

    /**
     * Sends a push notification to a subscribed user.
     * Automatically removes invalid subscriptions from the database.
     * @param sub Push notification subscription details
     * @param title Notification title
     * @param body Notification body
     */
    public void sendPush(PushNotifications sub, String title, String body) {
        if (sub == null
                || sub.getEndpoint() == null
                || sub.getP256dh() == null
                || sub.getAuth() == null) {
            logger.warn("Attempted to send push to an incomplete subscription: {}", sub);
            return;
        }

        try {
            logger.info("Preparing to send Web Push notification to: {}", sub.getEndpoint());

            Subscription subscription = new Subscription();
            subscription.endpoint = sub.getEndpoint();

            Subscription.Keys keys = new Subscription.Keys();
            keys.p256dh = sub.getP256dh();
            keys.auth = sub.getAuth();
            subscription.keys = keys;

            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> payload = new HashMap<>();
            payload.put("title", title);
            payload.put("body", body);
            String jsonPayload = mapper.writeValueAsString(payload);

            Notification notification = new Notification(subscription, jsonPayload, Urgency.HIGH);

            pushService.send(notification);

            logger.info("Successfully sent Web Push notification to: {}", sub.getEndpoint());
        } catch (IOException
                | InterruptedException
                | GeneralSecurityException
                | ExecutionException
                | JoseException e) {
            logger.error("Failed to send Web Push notification to: {}", sub.getEndpoint(), e);
            if (e.getMessage() != null
                    && (e.getMessage().contains("410") || e.getMessage().contains("404"))) {

                logger.warn(
                        "Subscription expired or invalid, removing from database: {}",
                        sub.getEndpoint());

                if (pushRepository != null && sub.getId() != null) {
                    try {
                        pushRepository.deleteById(sub.getId());
                        logger.info(
                                "Successfully removed invalid subscription from database: {}",
                                sub.getId());
                    } catch (Exception deleteEx) {
                        logger.error(
                                "Failed to delete invalid subscription: {}", deleteEx.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Sends a notification to all subscriptions of a user.
     * Iterates through all push subscriptions for the given user and sends the notification.
     * @param userId The ID of the user to send notifications to
     * @param title The notification title
     * @param body The notification body
     */
    public void sendNotificationToUser(UUID userId, String title, String body) {
        pushRepository.findAllByUserId(userId).forEach(sub -> sendPush(sub, title, body));
    }

    /**
     * Sends a notification to a user who has parked in a B-spot.
     * Notifies the user that they have parked someone in at a specific A-spot.
     * @param userName The name of the user who parked
     * @param aSpot The A-spot number where the user was parked in
     * @param userId The ID of the user to send the notification to
     */
    public void sendBSpotNotification(String userName, String aSpot, UUID userId) {
        String title = "You parked someone in!";
        String body = "You parked in " + userName + " at spot " + aSpot;
        sendNotificationToUser(userId, title, body);
    }

    /**
     * Sends a notification to a user who has been parked in at an A-spot.
     * Notifies the user that someone has parked them in.
     * @param userName The name of the user who parked them in
     * @param userId The ID of the user to send the notification to
     */
    public void sendASpotNotification(String userName, UUID userId) {
        String title = "You've been parked in!";
        String body = "You were parked in by " + userName;
        sendNotificationToUser(userId, title, body);
    }

    /**
     * Sends a notification for a regular spot reservation.
     * Notifies the user that they have successfully reserved a parking spot.
     * @param spot The spot number that was reserved
     * @param userName The name of the user who made the reservation
     * @param userId The ID of the user to send the notification to
     */
    public void sendRegularReservationNotification(String spot, String userName, UUID userId) {
        String title = "Spot " + spot + " reserved!";
        String body = "You've reserved this spot for yourself, " + userName;
        sendNotificationToUser(userId, title, body);
    }
}
