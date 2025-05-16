package no.bachelorgroup13.backend.features.user.repository;

import java.util.Optional;
import java.util.UUID;
import no.bachelorgroup13.backend.features.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing user data.
 * Provides methods to find users by various criteria in the database.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    /**
     * Finds a user by their primary license plate.
     * @param licensePlate The license plate to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByLicensePlate(String licensePlate);

    /**
     * Finds a user by their secondary license plate.
     * @param secondLicensePlate The secondary license plate to search for
     * @return Optional containing the user if found
     */
    Optional<User> findBySecondLicensePlate(String secondLicensePlate);

    /**
     * Finds a user by their email address.
     * @param email The email address to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by either their primary or secondary license plate.
     * @param licensePlate The license plate to search for
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM User u WHERE u.licensePlate = :plate OR u.secondLicensePlate = :plate")
    Optional<User> findByAnyLicensePlate(@Param("plate") String licensePlate);
}
