package no.bachelorgroup13.backend.common.config;

import org.springframework.context.annotation.Configuration;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;

/**
 * Configuration class for loading environment variables from .env file.
 * Sets up system properties for JWT, Computer Vision, and VAPID keys.
 */
@Configuration
public class DotenvConfig {

    /**
     * Loads environment variables from .env file and sets them as system properties.
     * Handles JWT, Computer Vision, and VAPID configuration.
     */
    @PostConstruct
    public void loadEnv() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().ignoreIfMalformed().load();

        if (dotenv.get("JWT_SECRET") != null) {
            System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET").trim());
        }

        if (dotenv.get("JWT_EXPIRATION") != null) {
            System.setProperty("JWT_EXPIRATION", dotenv.get("JWT_EXPIRATION").trim());
        }

        if (dotenv.get("COMPUTER_VISION_ENDPOINT") != null) {
            System.setProperty("COMPUTER_VISION_ENDPOINT", dotenv.get("COMPUTER_VISION_ENDPOINT"));
        }

        if (dotenv.get("COMPUTER_VISION_SUBSCRIPTION_KEY") != null) {
            System.setProperty(
                    "COMPUTER_VISION_SUBSCRIPTION_KEY",
                    dotenv.get("COMPUTER_VISION_SUBSCRIPTION_KEY"));
        }

        if (dotenv.get("VAPID_PUBLIC_KEY") != null) {
            System.setProperty("vapid.keys.public", dotenv.get("VAPID_PUBLIC_KEY").trim());
        }

        if (dotenv.get("VAPID_PRIVATE_KEY") != null) {
            System.setProperty("vapid.keys.private", dotenv.get("VAPID_PRIVATE_KEY").trim());
        }
    }
}
