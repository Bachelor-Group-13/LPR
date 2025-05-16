package no.bachelorgroup13.backend.features.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data transfer object for user registration requests.
 * Contains user details required for account creation.
 */
@Data
public class SignupRequest {
    @Schema(description = "The email of the user.")
    @NotBlank
    @Email
    private String email;

    @Schema(description = "The password of the user.")
    @NotBlank
    @Size(min = 6)
    private String password;

    @Schema(description = "The name of the user.")
    @NotBlank
    private String name;

    @Schema(description = "The license plate of the user.")
    private String licensePlate;

    @Schema(description = "The phone number of the user.")
    private String phoneNumber;
}
