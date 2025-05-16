package no.bachelorgroup13.backend.features.user.entity;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.bachelorgroup13.backend.features.auth.security.Role;

/**
 * Entity class representing a user in the system.
 * Maps to the 'users' table in the database.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Hidden
public class User {
    /**
     * Unique identifier for the user.
     */
    @Id
    @Column(name = "id")
    private UUID id;

    /**
     * User's email address. Must be unique and cannot be null.
     */
    @Email
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    /**
     * User's full name.
     */
    @Column(name = "name")
    private String name;

    /**
     * User's encrypted password. Cannot be null.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * User's primary vehicle license plate.
     */
    @Column(name = "license_plate")
    private String licensePlate;

    /**
     * User's secondary vehicle license plate, if applicable.
     */
    @Column(name = "second_license_plate")
    private String secondLicensePlate;

    /**
     * User's contact phone number.
     */
    @Column(name = "phone_number")
    private String phoneNumber;

    /**
     * Flag indicating whether the user account is enabled.
     */
    @Column(name = "enabled")
    private Boolean enabled;

    /**
     * User's role in the system. Defaults to ROLE_USER.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role = Role.ROLE_USER;
}
