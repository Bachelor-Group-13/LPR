package no.bachelorgroup13.backend.features.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.Data;

/**
 * Data transfer object for parking spot reservations.
 * Contains all necessary information about a reservation, including user and vehicle details.
 */
@Data
public class ReservationDto {
    @Schema(description = "The ID of the reservation.")
    private Integer id;

    @Schema(description = "The parking spot number.")
    private String spotNumber;

    @Schema(description = "The ID of the user who made the reservation.")
    private UUID userId;

    @Schema(description = "The date of the reservation.")
    private LocalDate reservationDate;

    @Schema(description = "The license plate of the vehicle.")
    private String licensePlate;

    @Schema(description = "The estimated departure time.")
    private ZonedDateTime estimatedDeparture;

    @Schema(description = "The name of the user.")
    private String userName;

    @Schema(description = "The email of the user.")
    private String userEmail;

    @Schema(description = "The phone number of the user.")
    private String userPhoneNumber;

    @Schema(description = "Whether the reservation is anonymous.")
    private boolean anonymous;

    @Schema(description = "Whether the spot is blocked.")
    private boolean blockedSpot;
}
