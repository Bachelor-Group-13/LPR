package no.bachelorgroup13.backend.features.reservation.mapper;

import org.springframework.stereotype.Component;

import no.bachelorgroup13.backend.features.reservation.dto.ReservationDto;
import no.bachelorgroup13.backend.features.reservation.entity.Reservation;

/**
 * Mapper for converting between Reservation entities and DTOs.
 * Handles the transformation of reservation data between layers.
 */
@Component
public class ReservationMapper {
    /**
     * Converts a Reservation entity to a ReservationDto.
     * Includes user details if available.
     * @param reservation The reservation entity to convert
     * @return The corresponding DTO, or null if input is null
     */
    public ReservationDto toDto(Reservation reservation) {
        if (reservation == null) {
            return null;
        }
        ReservationDto reservationDto = new ReservationDto();
        reservationDto.setId(reservation.getId());
        reservationDto.setSpotNumber(reservation.getSpotNumber());
        reservationDto.setUserId(reservation.getUserId());
        reservationDto.setReservationDate(reservation.getReservationDate());
        reservationDto.setLicensePlate(reservation.getLicensePlate());
        reservationDto.setAnonymous(Boolean.TRUE.equals(reservation.getAnonymous()));
        reservationDto.setBlockedSpot(Boolean.TRUE.equals(reservation.getBlockedSpot()));
        reservationDto.setEstimatedDeparture(
                reservation.getEstimatedDeparture() != null
                        ? reservation.getEstimatedDeparture()
                        : null);

        if (reservation.getUser() != null) {
            reservationDto.setUserName(reservation.getUser().getName());
            reservationDto.setUserEmail(reservation.getUser().getEmail());
            reservationDto.setUserPhoneNumber(reservation.getUser().getPhoneNumber());
        }
        return reservationDto;
    }

    /**
     * Converts a ReservationDto to a Reservation entity.
     * Handles anonymous reservations by setting userId to null.
     * @param reservationDto The DTO to convert
     * @return The corresponding entity, or null if input is null
     */
    public Reservation toEntity(ReservationDto reservationDto) {
        if (reservationDto == null) {
            return null;
        }
        Reservation reservation = new Reservation();
        reservation.setId(reservationDto.getId());
        reservation.setSpotNumber(reservationDto.getSpotNumber());

        if (Boolean.TRUE.equals(reservationDto.isAnonymous())) {
            reservation.setUserId(null);
        } else {
            reservation.setUserId(reservationDto.getUserId());
        }
        reservation.setReservationDate(reservationDto.getReservationDate());
        reservation.setLicensePlate(reservationDto.getLicensePlate());
        reservation.setAnonymous(Boolean.TRUE.equals(reservationDto.isAnonymous()));
        reservation.setBlockedSpot(Boolean.TRUE.equals(reservationDto.isBlockedSpot()));
        if (reservationDto.getEstimatedDeparture() != null) {
            reservation.setEstimatedDeparture(reservationDto.getEstimatedDeparture());
        }
        return reservation;
    }
}
