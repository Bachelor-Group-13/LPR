package no.bachelorgroup13.backend.features.push.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import no.bachelorgroup13.backend.features.push.entity.PushNotifications;
import no.bachelorgroup13.backend.features.push.repository.PushSubscriptionRepository;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WebPushService {
    private static final Logger logger = LoggerFactory.getLogger(WebPushService.class);
    private final PushService pushService;
    private final PushSubscriptionRepository pushRepository;
    private final ObjectMapper objectMapper;

    public WebPushService(
            @Value("${vapid.keys.public}") String publicKey,
            @Value("${vapid.keys.private}") String privateKey,
            PushSubscriptionRepository pushRepository) {
        this.pushRepository = pushRepository;
        this.objectMapper = new ObjectMapper();

        Security.addProvider(new BouncyCastleProvider());

        try {
            this.pushService = new PushService(publicKey, privateKey);
            this.pushService.setSubject("https://129.241.152.242.nip.io/");

            logger.info("Web Push Service initialized successfully with public key: {}", publicKey);
        } catch (Exception e) {
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
            logger.debug("p256dh value: {}", sub.getP256dh());
            logger.debug("auth value: {}", sub.getAuth());


            String jsonPayload = "{\"title\":\"" + title + "\",\"body\":\"" + body + "\"}";
            logger.debug("Notification payload: {}", jsonPayload);

            Notification notification = new Notification(subscription, jsonPayload);

            pushService.send(notification);

            logger.info("Successfully sent Web Push notification to: {}", sub.getEndpoint());
        } catch (Exception e) {
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
