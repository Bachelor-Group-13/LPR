package no.bachelorgroup13.backend.features.auth.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data transfer object for JWT authentication responses.
 * Contains user information and authentication tokens.
 */
@Data
@AllArgsConstructor
public class JwtResponse {
    @Schema(description = "The JWT token.")
    private String token;

    @Schema(description = "The type of the token.")
    private String type = "Bearer";

    @Schema(description = "The ID of the user.")
    private UUID id;

    @Schema(description = "The email of the user.")
    private String email;

    @Schema(description = "The name of the user.")
    private String name;

    @Schema(description = "The refresh token.")
    private String refreshToken;
}
