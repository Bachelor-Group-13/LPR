package no.bachelorgroup13.backend.features.reservation.entity;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.bachelorgroup13.backend.features.user.entity.User;

/**
 * Entity representing a parking spot reservation.
 * Stores reservation details including user information and spot status.
 */
@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    /**
     * Unique identifier for the reservation.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * The parking spot number for this reservation.
     */
    @Column(name = "spot_number")
    private String spotNumber;

    /**
     * The ID of the user who made the reservation.
     * Can be null for anonymous reservations.
     */
    @Column(name = "user_id", insertable = true, updatable = true, nullable = true)
    private UUID userId;

    /**
     * The date when the spot is reserved.
     */
    @Column(name = "reservation_date")
    private LocalDate reservationDate;

    /**
     * The license plate of the vehicle.
     * Can be null for anonymous reservations.
     */
    @Column(name = "license_plate", nullable = true)
    private String licensePlate;

    /**
     * The user entity associated with this reservation.
     * Eagerly loaded and optional for anonymous reservations.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * The estimated time when the vehicle will leave the spot.
     * Can be null if not specified.
     */
    @Column(name = "estimated_departure", nullable = true)
    private ZonedDateTime estimatedDeparture;

    /**
     * Whether this is an anonymous reservation.
     * Defaults to false.
     */
    @Column(name = "is_anonymous", nullable = false)
    private Boolean anonymous = false;

    /**
     * Whether the spot is blocked by another vehicle.
     * Defaults to false.
     */
    @Column(name = "is_blocked_spot", nullable = false)
    private Boolean blockedSpot = false;
}
