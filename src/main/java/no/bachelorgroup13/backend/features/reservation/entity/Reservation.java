package no.bachelorgroup13.backend.features.reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.bachelorgroup13.backend.features.user.entity.User;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "spot_number")
    private String spotNumber;

    @Column(name = "user_id", insertable = true, updatable = true, nullable = true)
    private UUID userId;

    @Column(name = "reservation_date")
    private LocalDate reservationDate;

    @Column(name = "license_plate", nullable = true)
    private String licensePlate;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "estimated_departure", nullable = true)
    private ZonedDateTime estimatedDeparture;

    @Column(name = "is_anonymous", nullable = false)
    private Boolean anonymous = false;

    @Column(name = "is_blocked_spot", nullable = false)
    private Boolean blockedSpot = false;
}
