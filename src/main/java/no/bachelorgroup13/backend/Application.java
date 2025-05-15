package no.bachelorgroup13.backend;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import no.bachelorgroup13.backend.features.licenseplate.azurecv.LicensePlateProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(LicensePlateProperties.class)
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
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
            System.setProperty("vapid.keys.public", dotenv.get("VAPID_PUBLIC_KEY"));
        }

        if (dotenv.get("VAPID_PRIVATE_KEY") != null) {
            System.setProperty("vapid.keys.private", dotenv.get("VAPID_PRIVATE_KEY").trim());
        }

        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void logDatabaseConfig() {
        log.info("Database URL: {}", System.getenv("SPRING_DATASOURCE_URL"));
        log.info("Database Username: {}", System.getenv("SPRING_DATASOURCE_USERNAME"));
        log.info("Database Connection Test: Starting...");
        try {
            log.info("Database Connection Test: Successful");
        } catch (Exception e) {
            log.error("Database Connection Test: Failed", e);
        }
    }
}
