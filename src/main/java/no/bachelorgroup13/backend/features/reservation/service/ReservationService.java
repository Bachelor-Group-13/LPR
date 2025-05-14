package no.bachelorgroup13.backend.features.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import no.bachelorgroup13.backend.features.reservation.entity.Reservation;
import no.bachelorgroup13.backend.features.reservation.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Optional<Reservation> getReservationById(Integer id) {
        return reservationRepository.findById(id);
    }

    public List<Reservation> getReservationsByUserId(UUID userId) {
        return reservationRepository.findByUserId(userId);
    }

    public List<Reservation> getReservationsByDate(LocalDate date) {
        return reservationRepository.findByReservationDate(date);
    }

    public List<Reservation> getReservationsByLicensePlate(String licensePlate) {
        return reservationRepository.findByLicensePlate(licensePlate);
    }

    public List<Reservation> getReservationsBySpotNumber(String spotNumber) {
        return reservationRepository.findBySpotNumber(spotNumber);
    }

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

    public Reservation updateReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public void deleteReservation(Integer id) {
        reservationRepository.deleteById(id);
    }

    public Boolean hasActiveReservation(UUID userId) {
        LocalDate today = LocalDate.now();
        return reservationRepository.existsByUserIdAndReservationDate(userId, today);
    }

    @Transactional
    public void deleteAllReservations() {
        reservationRepository.deleteAll();
    }
}
