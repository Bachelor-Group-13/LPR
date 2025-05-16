package no.bachelorgroup13.backend.features.user.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.bachelorgroup13.backend.features.auth.security.Role;

/**
 * Data Transfer Object for user information.
 * Used to transfer user data between layers of the application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User Information")
public class UserDto {
    /**
     * Unique identifier for the user.
     */
    @Schema(description = "The ID of the user.")
    private UUID id;

    /**
     * User's full name.
     */
    @Schema(description = "The name of the user.")
    private String name;

    /**
     * User's email address.
     */
    @Schema(description = "The email of the user.")
    private String email;

    /**
     * User's contact phone number.
     */
    @Schema(description = "The phone number of the user.")
    private String phoneNumber;

    /**
     * User's primary vehicle license plate.
     */
    @Schema(description = "The license plate of the user.")
    private String licensePlate;

    /**
     * User's secondary vehicle license plate, if applicable.
     */
    @Schema(description = "The second license plate of the user (Optional).")
    private String secondLicensePlate;

    /**
     * User's role in the system (e.g., ROLE_USER, ROLE_DEVELOPER).
     */
    @Schema(description = "The role of the user.")
    private Role role;
}
