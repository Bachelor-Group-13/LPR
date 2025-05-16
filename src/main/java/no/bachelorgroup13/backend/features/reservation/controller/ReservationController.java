package no.bachelorgroup13.backend.features.reservation.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.bachelorgroup13.backend.features.push.repository.PushSubscriptionRepository;
import no.bachelorgroup13.backend.features.push.service.WebPushService;
import no.bachelorgroup13.backend.features.reservation.dto.ReservationDto;
import no.bachelorgroup13.backend.features.reservation.entity.Reservation;
import no.bachelorgroup13.backend.features.reservation.mapper.ReservationMapper;
import no.bachelorgroup13.backend.features.reservation.service.ReservationService;

/**
 * Controller for managing parking spot reservations.
 * Handles CRUD operations and notifications for reservations.
 */
@RestController
@Slf4j
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation", description = "Endpoints for managing reservations.")
public class ReservationController {

    private final ReservationService reservationService;
    private final PushSubscriptionRepository pushRepository;
    private final WebPushService pushService;
    private final ReservationMapper reservationMapper;

    /**
     * Retrieves all reservations.
     * @return List of all reservations
     */
    @Operation(summary = "Get all reservations")
    @GetMapping
    public ResponseEntity<List<ReservationDto>> getAllReservations() {
        return ResponseEntity.ok(
                reservationService.getAllReservations().stream()
                        .map(reservationMapper::toDto)
                        .collect((Collectors.toList())));
    }

    /**
     * Retrieves a reservation by its ID.
     * @param id Reservation ID
     * @return Reservation if found, 404 if not found
     */
    @Operation(summary = "Get reservation by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ReservationDto> getReservationById(@PathVariable Integer id) {
        return reservationService
                .getReservationById(id)
                .map(reservationMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all reservations for a specific user.
     * @param userId User ID
     * @return List of user's reservations
     */
    @Operation(summary = "Get reservations by user ID")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReservationDto>> getReservationsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(
                reservationService.getReservationsByUserId(userId).stream()
                        .map(reservationMapper::toDto)
                        .collect(Collectors.toList()));
    }

    /**
     * Retrieves all reservations for a specific date.
     * @param date Reservation date
     * @return List of reservations for the date
     */
    @Operation(summary = "Get reservations by date")
    @GetMapping("/date/{date}")
    public ResponseEntity<List<ReservationDto>> getReservationsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(
                reservationService.getReservationsByDate(date).stream()
                        .map(reservationMapper::toDto)
                        .collect(Collectors.toList()));
    }

    /**
     * Retrieves all reservations for a specific license plate.
     * @param licensePlate License plate number
     * @return List of reservations for the license plate
     */
    @Operation(summary = "Get reservations by license plate")
    @GetMapping("/license-plate/{licensePlate}")
    public ResponseEntity<List<ReservationDto>> getReservationsByLicensePlate(
            @PathVariable String licensePlate) {
        return ResponseEntity.ok(
                reservationService.getReservationsByLicensePlate(licensePlate).stream()
                        .map(reservationMapper::toDto)
                        .collect(Collectors.toList()));
    }

    /**
     * Creates a new reservation.
     * Sends push notifications for non-anonymous reservations.
     * @param reservationDto Reservation details
     * @return Created reservation or error message
     */
    @Operation(summary = "Get reservations by spot number")
    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody ReservationDto reservationDto) {
        log.info("Received reservation request: {}", reservationDto);

        try {
            if (!reservationDto.isAnonymous()) {
                if (reservationDto.getUserId() == null) {
                    return ResponseEntity.badRequest()
                            .body("User ID is required for non-anonymous reservations");
                }

                if (reservationService.hasActiveReservation(reservationDto.getUserId())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("User already has an active reservation");
                }
            }

            if (reservationDto.getSpotNumber() == null
                    || reservationDto.getReservationDate() == null) {
                return ResponseEntity.badRequest()
                        .body("Spot number and reservation date are required");
            }

            Reservation reservation = reservationMapper.toEntity(reservationDto);
            Reservation saved = reservationService.createReservation(reservation);
            ReservationDto savedDto = reservationMapper.toDto(saved);

            if (!reservationDto.isAnonymous() && reservationDto.getUserId() != null) {
                try {
                    sendPushNotification(saved, false);
                } catch (Exception e) {
                    log.error("Failed to send push notification", e);
                }
            }

            log.info("Successfully created reservation: {}", saved);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);

        } catch (IllegalArgumentException e) {
            log.error("Invalid reservation request", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("Conflict while creating reservation", e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while creating reservation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create reservation: " + e.getMessage());
        }
    }

    /**
     * Sends push notifications to users about their reservations.
     * @param reservation The reservation to notify about
     * @param isParkedIn Whether the notification is about being parked in
     */
    @Operation(summary = "Send push notification")
    private void sendPushNotification(Reservation reservation, boolean isParkedIn) {
        pushRepository
                .findAllByUserId(reservation.getUserId())
                .forEach(
                        sub -> {
                            String title =
                                    isParkedIn
                                            ? "You've been parked in!"
                                            : "Spot " + reservation.getSpotNumber() + " reserved!";
                            String body =
                                    isParkedIn
                                            ? "Someone has parked in front of your car in spot "
                                                    + reservation.getSpotNumber()
                                            : "Check if you parked in someone";
                            pushService.sendPush(sub, title, body);
                        });
    }

    /**
     * Updates an existing reservation.
     * Handles special cases for B-spots and sends notifications.
     * @param id Reservation ID
     * @param reservationDto Updated reservation details
     * @return Updated reservation or 404 if not found
     */
    @Operation(summary = "Update reservation")
    @PutMapping("/{id}")
    public ResponseEntity<ReservationDto> updateReservation(
            @PathVariable Integer id, @RequestBody ReservationDto reservationDto) {
        return reservationService
                .getReservationById(id)
                .map(
                        existingReservation -> {
                            Reservation reservation = reservationMapper.toEntity(reservationDto);
                            reservation.setId(id);

                            boolean isBSpot = reservation.getSpotNumber().endsWith("B");

                            if (isBSpot && reservation.getLicensePlate() != null) {
                                reservation.setBlockedSpot(true);

                                String rowNumber =
                                        reservation
                                                .getSpotNumber()
                                                .substring(
                                                        0,
                                                        reservation.getSpotNumber().length() - 1);
                                String aSpotNumber = rowNumber + "A";

                                reservationService.getReservationsBySpotNumber(aSpotNumber).stream()
                                        .filter(r -> r.getReservationDate().equals(LocalDate.now()))
                                        .filter(r -> !r.getAnonymous() && r.getUserId() != null)
                                        .findFirst()
                                        .ifPresent(
                                                aSpotReservation -> {
                                                    try {
                                                        sendPushNotification(
                                                                aSpotReservation, true);
                                                    } catch (Exception e) {
                                                        log.error(
                                                                "Failed to send parked-in"
                                                                        + " notification",
                                                                e);
                                                    }
                                                });
                            } else {
                                reservation.setBlockedSpot(false);
                            }

                            Reservation updated = reservationService.updateReservation(reservation);
                            return ResponseEntity.ok(reservationMapper.toDto(updated));
                        })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a reservation by ID.
     * @param id Reservation ID
     * @return 204 No Content if successful, 404 if not found
     */
    @Operation(summary = "Delete reservation")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Integer id) {
        return reservationService
                .getReservationById(id)
                .map(
                        reservation -> {
                            reservationService.deleteReservation(id);
                            return ResponseEntity.noContent().<Void>build();
                        })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes all reservations.
     * Requires ROLE_DEVELOPER authority.
     * @return 204 No Content if successful
     */
    @Operation(summary = "Delete all reservations")
    @DeleteMapping("/all")
    @PreAuthorize("hasRole('ROLE_DEVELOPER')")
    public ResponseEntity<Void> deleteAllReservations() {
        reservationService.deleteAllReservations();
        return ResponseEntity.noContent().build();
    }
}
