package no.bachelorgroup13.backend.features.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import no.bachelorgroup13.backend.features.reservation.entity.Reservation;

/**
 * Repository for managing parking spot reservations.
 * Provides methods to find and query reservations in the database.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    /**
     * Finds all reservations for a specific user.
     * @param userId The ID of the user
     * @return List of user's reservations
     */
    List<Reservation> findByUserId(UUID userId);

    /**
     * Finds all reservations for a specific date.
     * @param date The reservation date
     * @return List of reservations for the date
     */
    List<Reservation> findByReservationDate(LocalDate date);

    /**
     * Finds all reservations for a specific license plate.
     * @param licensePlate The license plate number
     * @return List of reservations for the license plate
     */
    List<Reservation> findByLicensePlate(String licensePlate);

    /**
     * Checks if a user has a reservation for a specific date.
     * @param userId The ID of the user
     * @param reservationDate The date to check
     * @return true if a reservation exists, false otherwise
     */
    boolean existsByUserIdAndReservationDate(UUID userId, LocalDate reservationDate);

    /**
     * Finds all reservations for a specific parking spot.
     * @param spotNumber The parking spot number
     * @return List of reservations for the spot
     */
    List<Reservation> findBySpotNumber(String spotNumber);
}
