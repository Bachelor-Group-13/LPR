package no.bachelorgroup13.backend.features.push.service;

import com.google.gson.Gson;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.Map;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import no.bachelorgroup13.backend.features.push.entity.PushNotifications;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PushServiceWrapper {
    private static final Logger logger = LoggerFactory.getLogger(PushServiceWrapper.class);

    private final PushService pushService;

    public PushServiceWrapper(
            @Value("${vapid.keys.public}") String publicKey,
            @Value("${vapid.keys.private}") String privateKey)
            throws GeneralSecurityException {

        Security.addProvider(new BouncyCastleProvider());

        String safePublicKey = publicKey.replace('+', '-').replace('/', '_').replaceAll("=+$", "");

        String safePrivateKey =
                privateKey.replace('+', '-').replace('/', '_').replaceAll("=+$", "");

        logger.info(
                "Initializing PushService with VAPID public key: {}…",
                safePublicKey.substring(0, 10));
        this.pushService = new PushService(safePublicKey, safePrivateKey);
    }

    public void sendPush(PushNotifications sub, String title, String body)
            throws GeneralSecurityException, IOException {
        String payload = new Gson().toJson(Map.of("title", title, "body", body));

        String userKey = sub.getP256dh().replace('+', '-').replace('/', '_').replaceAll("=+$", "");
        String authSecret = sub.getAuth().replace('+', '-').replace('/', '_').replaceAll("=+$", "");

        Notification notification =
                new Notification(sub.getEndpoint(), userKey, authSecret, payload);

        try {
            logger.info(
                    "Sending push → endpoint: {}  payload: {}…",
                    sub.getEndpoint(),
                    payload.substring(0, Math.min(payload.length(), 50)));
            HttpResponse response = pushService.send(notification);
            logger.info(
                    "FCM response status: {} {}",
                    response.getStatusLine().getStatusCode(),
                    response.getStatusLine().getReasonPhrase());
        } catch (Exception e) {
            logger.error("Failed to send push to {}:", sub.getEndpoint(), e);
            throw new RuntimeException(e);
        }
    }
}
