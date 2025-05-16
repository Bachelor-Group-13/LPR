package no.bachelorgroup13.backend.features.push.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import no.bachelorgroup13.backend.features.auth.security.CustomUserDetails;
import no.bachelorgroup13.backend.features.push.dto.PushSubscriptionDto;
import no.bachelorgroup13.backend.features.push.entity.PushNotifications;
import no.bachelorgroup13.backend.features.push.repository.PushSubscriptionRepository;
import no.bachelorgroup13.backend.features.push.service.WebPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/push")
@Tag(name = "Push", description = "Endpoints for managing push notifications.")
public class PushController {
    private final Logger logger = LoggerFactory.getLogger(PushController.class);
    private final PushSubscriptionRepository repository;
    private final WebPushService webPushService;

    @Value("${vapid.keys.public}")
    private String vapidPublicKey;

    public PushController(PushSubscriptionRepository repository, WebPushService webPushService) {
        this.repository = repository;
        this.webPushService = webPushService;
    }

    @Operation(summary = "Get VAPID public key")
    @GetMapping("/publicKey")
    public ResponseEntity<String> publicKey() {
        return ResponseEntity.ok(vapidPublicKey);
    }

    @Operation(summary = "Subscribe to push notifications")
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(
            @RequestBody PushSubscriptionDto dto, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            logger.info("Processing subscription request for endpoint: {}", dto.getEndpoint());
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            if (dto.getEndpoint() == null || dto.getP256dh() == null || dto.getAuth() == null) {
                logger.error("Missing required subscription data");
                return ResponseEntity.badRequest().body("Missing required subscription data");
            }

            Optional<PushNotifications> existingSubscription =
                    repository.findByEndpoint(dto.getEndpoint());

            PushNotifications pushNotifications;
            if (existingSubscription.isPresent()) {
                pushNotifications = existingSubscription.get();
                pushNotifications.setP256dh(dto.getP256dh());
                pushNotifications.setAuth(dto.getAuth());
                pushNotifications.setUserId(userDetails.getId());
                logger.info(
                        "Updating existing push notification subscription for user: {}",
                        userDetails.getId());
            } else {
                pushNotifications = new PushNotifications();
                pushNotifications.setEndpoint(dto.getEndpoint());
                pushNotifications.setP256dh(dto.getP256dh());
                pushNotifications.setAuth(dto.getAuth());
                pushNotifications.setUserId(userDetails.getId());
                logger.info(
                        "Creating new push notification subscription for user: {}",
                        userDetails.getId());
            }

            repository.save(pushNotifications);
            return ResponseEntity.ok().body("Subscription saved successfully");
        } catch (Exception e) {
            logger.error("Error saving subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving subscription: " + e.getMessage());
        }
    }

    @Operation(summary = "Server-side push smoke test")
    @GetMapping("/test")
    public ResponseEntity<Map<String, Integer>> testPush(Authentication auth) {
        UUID userId = ((CustomUserDetails) auth.getPrincipal()).getId();
        List<PushNotifications> subs = repository.findAllByUserId(userId);
        logger.info("TEST PUSH: found {} subscriptions for user {}", subs.size(), userId);
        subs.forEach(
                sub -> {
                    try {
                        webPushService.sendPush(
                                sub,
                                "🅿️ Parking Manager Test",
                                "If you see this, server-side works!");
                    } catch (Exception e) {
                        logger.error(
                                "Test push failed for {}: {}", sub.getEndpoint(), e.getMessage());
                    }
                });
        return ResponseEntity.ok(Map.of("sent", subs.size()));
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(
            @RequestBody Map<String, String> body, Authentication auth) {
        repository.findByEndpoint(body.get("endpoint")).ifPresent(repository::delete);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test-cli")
    public ResponseEntity<String> testCliPush() {
        try {
            PushNotifications pushNotification = new PushNotifications();
            pushNotification.setEndpoint(
                    "https://fcm.googleapis.com/fcm/send/fchvyGJBhik:APA91bFRNksx5F5Yz-JeI26RIDw9-_1UYmZE5rPh9xt1iVBuZgRXLxy6QQ1vTGzrFjIHUuJ39Su8kYpObPczXa4yU-Gc56Izwbqi_4PxTtRvkXdWTiSQ6XC_vS7tBjJHTzBhU8ZDZw-l");
            pushNotification.setP256dh(
                    "BCRtawoGeUy/3muV/Ylv6cwmVbWlaZ3YMD7iN7Bi6/+exTBsaDtQGkfsouZWtupHtnD1v1Vn24lZBuT3zaSMkhM=");
            pushNotification.setAuth("Xy+YwWQhGi1FHSQdStuVoQ==");

            webPushService.sendPush(pushNotification, "CLI Test", "Hello from CLI");

            return ResponseEntity.ok("Test push sent with CLI values");
        } catch (Exception e) {
            logger.error("Test push failed: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
