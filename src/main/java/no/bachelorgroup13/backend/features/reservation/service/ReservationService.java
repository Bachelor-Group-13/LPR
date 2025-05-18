package no.bachelorgroup13.backend.features.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import no.bachelorgroup13.backend.features.push.service.WebPushService;
import no.bachelorgroup13.backend.features.reservation.entity.Reservation;
import no.bachelorgroup13.backend.features.reservation.repository.ReservationRepository;
import no.bachelorgroup13.backend.features.user.entity.User;
import no.bachelorgroup13.backend.features.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing parking spot reservations.
 * Handles CRUD operations and business logic for reservations.
 */
@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final WebPushService webPushService;
    private final UserRepository userRepository;

    /**
     * Retrieves all reservations in the system.
     * @return List of all reservations
     */
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    /**
     * Retrieves a reservation by its ID.
     * @param id The reservation ID
     * @return Optional containing the reservation if found
     */
    public Optional<Reservation> getReservationById(Integer id) {
        return reservationRepository.findById(id);
    }

    /**
     * Retrieves all reservations for a specific user.
     * @param userId The user's ID
     * @return List of user's reservations
     */
    public List<Reservation> getReservationsByUserId(UUID userId) {
        return reservationRepository.findByUserId(userId);
    }

    /**
     * Retrieves all reservations for a specific date.
     * @param date The reservation date
     * @return List of reservations for the date
     */
    public List<Reservation> getReservationsByDate(LocalDate date) {
        return reservationRepository.findByReservationDate(date);
    }

    /**
     * Retrieves all reservations for a specific license plate.
     * @param licensePlate The license plate number
     * @return List of reservations for the license plate
     */
    public List<Reservation> getReservationsByLicensePlate(String licensePlate) {
        return reservationRepository.findByLicensePlate(licensePlate);
    }

    /**
     * Retrieves all reservations for a specific parking spot.
     * @param spotNumber The parking spot number
     * @return List of reservations for the spot
     */
    public List<Reservation> getReservationsBySpotNumber(String spotNumber) {
        return reservationRepository.findBySpotNumber(spotNumber);
    }

    /**
     * Creates a new reservation.
     * Sets default values for anonymous and blockedSpot if not provided.
     * @param reservation The reservation to create
     * @return The created reservation
     */
    @Transactional
    public Reservation createReservation(Reservation reservation) {
        if (reservation.getAnonymous() == null) {
            reservation.setAnonymous(false);
        }

        if (reservation.getBlockedSpot() == null) {
            reservation.setBlockedSpot(false);
        }
        return reservationRepository.save(reservation);
    }

    /**
     * Updates an existing reservation.
     * @param reservation The reservation to update
     * @return The updated reservation
     */
    public Reservation updateReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    /**
     * Deletes a reservation by its ID.
     * @param id The ID of the reservation to delete
     */
    public void deleteReservation(Integer id) {
        reservationRepository.deleteById(id);
    }

    /**
     * Checks if a user has an active reservation for today.
     * @param userId The user's ID
     * @return true if a user has an active reservation, false otherwise
     */
    public Boolean hasActiveReservation(UUID userId) {
        LocalDate today = LocalDate.now();
        return reservationRepository.existsByUserIdAndReservationDate(userId, today);
    }

    /**
     * Deletes all reservations in the system.
     * Use with caution as this operation cannot be undone.
     */
    @Transactional
    public void deleteAllReservations() {
        reservationRepository.deleteAll();
    }

    /**
     * Handles notifications for a new or updated reservation.
     * Sends appropriate notifications based on the spot type (B-spot or regular spot).
     * @param reservation The reservation to handle notifications for
     */
    public void handleReservationNotifications(Reservation reservation) {
        String spot = reservation.getSpotNumber();
        UUID userId = reservation.getUserId();
        String userName = getUserName(userId);

        if (spot.endsWith("B")) {
            handleBSpotNotifications(reservation, userName);
        } else {
            webPushService.sendRegularReservationNotification(spot, userName, userId);
        }
    }

    /**
     * Handles notifications for B-spot reservations.
     * Sends notifications to both the B-spot user and the corresponding A-spot user if one exists.
     * @param reservation The B-spot reservation
     * @param parkerName The name of the user who made the reservation
     */
    private void handleBSpotNotifications(Reservation reservation, String parkerName) {
        String row =
                reservation.getSpotNumber().substring(0, reservation.getSpotNumber().length() - 1);
        String aSpot = row + "A";
        UUID parkerId = reservation.getUserId();

        getReservationsBySpotNumber(aSpot).stream()
                .filter(r -> r.getReservationDate().equals(LocalDate.now()))
                .filter(r -> !Boolean.TRUE.equals(r.getAnonymous()) && r.getUserId() != null)
                .findFirst()
                .ifPresent(
                        aRes -> {
                            String aSpotUserName = getUserName(aRes.getUserId());

                            webPushService.sendBSpotNotification(aSpotUserName, aSpot, parkerId);

                            webPushService.sendASpotNotification(parkerName, aRes.getUserId());
                        });
    }

    /**
     * Retrieves a user's name from the user repository.
     * Returns "someone" if the user is not found.
     * @param userId The ID of the user to look up
     * @return The user's name or "someone" if not found
     */
    private String getUserName(UUID userId) {
        return userRepository.findById(userId).map(User::getName).orElse("someone");
    }
}
