package no.bachelorgroup13.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import no.bachelorgroup13.backend.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
  List<Reservation> findByUserId(UUID userId);

  List<Reservation> findByReservationDate(LocalDate date);

  List<Reservation> findByLicensePlate(String licensePlate);

  boolean existsByUserIdAndIsActive(UUID userId, LocalDate after, LocalDate equal);
}
