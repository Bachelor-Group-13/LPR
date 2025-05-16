package no.bachelorgroup13.backend.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for JWT (JSON Web Token) settings.
 * Manages secret key and token expiration times.
 */
@Configuration
@Data
public class JwtConfig {
    /** JWT secret key used for signing tokens */
    @Value("${app.jwt.secret}")
    private String secret;

    /** Access token expiration time in milliseconds */
    private long expiration = 84000000;

    /** Refresh token expiration time in milliseconds */
    private long refreshExpiration = 604800000;
}
