package no.bachelorgroup13.backend.features.push.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.concurrent.ExecutionException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import nl.martijndwars.webpush.Urgency;
import no.bachelorgroup13.backend.features.push.entity.PushNotifications;
import no.bachelorgroup13.backend.features.push.repository.PushSubscriptionRepository;

@Service
public class WebPushService {
    private static final Logger logger = LoggerFactory.getLogger(WebPushService.class);
    private final PushService pushService;
    private final PushSubscriptionRepository pushRepository;

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
    
            String jsonPayload = String.format(
                "{\"title\":\"%s\",\"body\":\"%s\"}",
                title.replace("\"", "\\\""),
                body.replace("\"", "\\\"")
            );            logger.debug("Notification payload: {}", jsonPayload);
    
            Notification notification = new Notification(subscription, jsonPayload, Urgency.HIGH);
    
            pushService.send(notification);
    
            logger.info("Successfully sent Web Push notification to: {}", sub.getEndpoint());
        } catch (IOException | InterruptedException | GeneralSecurityException | ExecutionException | JoseException e) {
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
}
