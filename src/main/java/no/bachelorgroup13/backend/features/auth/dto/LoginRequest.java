package no.bachelorgroup13.backend.features.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Data transfer object for user login requests.
 * Contains email and password for authentication.
 */
@Data
public class LoginRequest {
    @Schema(description = "The email of the user.")
    @NotBlank
    @Email
    private String email;

    @Schema(description = "The password of the user.")
    @NotBlank
    private String password;
}
