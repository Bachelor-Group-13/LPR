package no.bachelorgroup13.backend.features.push.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for web push notification subscriptions.
 * Contains the necessary information for subscribing to push notifications.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushSubscriptionDto {
    @Schema(description = "The endpoint URL for the push subscription.")
    @NotNull(message = "Endpoint is required")
    private String endpoint;

    @Schema(description = "The public key for the push subscription.")
    @NotNull(message = "P256dh key is required")
    private String p256dh;

    @Schema(description = "The authentication secret for the push subscription.")
    @NotNull(message = "Auth secret is required")
    private String auth;

    @Schema(description = "The ID of the user associated with the push subscription.")
    private UUID userId;

    /**
     * Returns a string representation of the subscription details.
     * Masks sensitive information (p256dh and auth) for security.
     * @return String representation of the subscription
     */
    @Override
    public String toString() {
        return "PushSubscriptionDto{"
                + "endpoint='"
                + endpoint
                + '\''
                + ", p256dh='"
                + (p256dh != null ? "[SET]" : "[NOT SET]")
                + '\''
                + ", auth='"
                + (auth != null ? "[SET]" : "[NOT SET]")
                + '\''
                + '}';
    }
}
