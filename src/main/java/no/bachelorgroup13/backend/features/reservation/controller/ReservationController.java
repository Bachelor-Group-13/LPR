package no.bachelorgroup13.backend.features.reservation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.bachelorgroup13.backend.features.reservation.dto.ReservationDto;
import no.bachelorgroup13.backend.features.reservation.entity.Reservation;
import no.bachelorgroup13.backend.features.reservation.mapper.ReservationMapper;
import no.bachelorgroup13.backend.features.reservation.service.ReservationService;
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
     * @param dto The reservation data to create
     * @return ResponseEntity containing the created reservation DTO or error message
     */
    @PostMapping
    @Operation(summary = "Create a new reservation (and send notifications)")
    public ResponseEntity<ReservationDto> createReservation(@RequestBody ReservationDto dto) {
        log.info("Received reservation request: {}", dto);
        try {
            Reservation toSave = reservationMapper.toEntity(dto);
            Reservation saved = reservationService.createReservation(toSave);
            reservationService.handleReservationNotifications(saved);

            return ResponseEntity.status(HttpStatus.CREATED).body(reservationMapper.toDto(saved));
        } catch (Exception e) {
            log.error("Error creating reservation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates an existing reservation.
     * Sends notifications if the updated reservation is for a B-spot with a license plate.
     * @param id The ID of the reservation to update
     * @param dto The updated reservation data
     * @return ResponseEntity containing the updated reservation DTO if found, 404 if not found
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update reservation (and send notifications on B-spot)")
    public ResponseEntity<ReservationDto> updateReservation(
            @PathVariable Integer id, @RequestBody ReservationDto dto) {
        return reservationService
                .getReservationById(id)
                .map(
                        existing -> {
                            Reservation toUpdate = reservationMapper.toEntity(dto);
                            toUpdate.setId(id);
                            Reservation updated = reservationService.updateReservation(toUpdate);

                            if (updated.getSpotNumber().endsWith("B")
                                    && updated.getLicensePlate() != null) {
                                reservationService.handleReservationNotifications(updated);
                            }

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
