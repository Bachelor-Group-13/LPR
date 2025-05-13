package no.bachelorgroup13.backend.features.user.repository;

import java.util.Optional;
import java.util.UUID;
import no.bachelorgroup13.backend.features.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByLicensePlate(String licensePlate);

    Optional<User> findBySecondLicensePlate(String secondLicensePlate);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.licensePlate = :plate OR u.secondLicensePlate = :plate")
    Optional<User> findByAnyLicensePlate(@Param("plate") String licensePlate);
}
