package no.bachelorgroup13.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Configuration class for OpenAPI/Swagger documentation.
 * Sets up API information and documentation endpoints.
 */
@Configuration
public class OpenApiConfig {
    /**
     * Creates and configures the OpenAPI documentation.
     * @return OpenAPI configuration with title, description and version
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Inneparkert API")
                                .description("API for the Inneparkert app")
                                .version("1.0.0"));
    }
}
