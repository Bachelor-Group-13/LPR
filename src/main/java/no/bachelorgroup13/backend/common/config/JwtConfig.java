package no.bachelorgroup13.backend.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class JwtConfig {
    @Value("${app.jwt.secret}")
    private String secret;

    private long expiration = 84000000;
    private long refreshExpiration = 604800000;

    public String getSecret() {
        System.out.println("JWT secret: " + secret);
        return secret;
    }

    public void setRefreshExpiration(long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }
}
