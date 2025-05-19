package no.bachelorgroup13.backend.features.user.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import no.bachelorgroup13.backend.features.auth.security.Role;
import no.bachelorgroup13.backend.features.user.entity.User;
import no.bachelorgroup13.backend.features.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for managing user operations.
 * Handles CRUD operations and business logic for users.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieves all users in the system.
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Retrieves a user by their ID.
     * @param id The user's ID
     * @return Optional containing the user if found
     */
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    /**
     * Retrieves a user by their license plate.
     * Checks both primary and secondary license plates.
     * @param licensePlate The license plate to search for
     * @return Optional containing the user if found
     */
    public Optional<User> getUserByLicensePlate(String licensePlate) {
        Optional<User> user = userRepository.findByLicensePlate(licensePlate);
        if (user.isPresent()) {
            return user;
        }
        return userRepository.findBySecondLicensePlate(licensePlate);
    }

    /**
     * Creates a new user.
     * Generates a UUID if not provided and sets default role if not specified.
     * @param user The user to create
     * @return The created user
     */
    public User createUser(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
        }
        if (user.getRole() == null) {
            user.setRole(Role.ROLE_USER);
        }
        return userRepository.save(user);
    }

    /**
     * Updates an existing user.
     * Preserves existing values for null fields and handles password encryption.
     * @param updatedUser The updated user data
     * @return The updated user
     * @throws RuntimeException if a user is not found
     */
    public User updateUser(User updatedUser) {
        return userRepository
                .findById(updatedUser.getId())
                .map(
                        existingUser -> {
                            if (updatedUser.getLicensePlate() != null) {
                                existingUser.setLicensePlate(updatedUser.getLicensePlate());
                            }

                            if (updatedUser.getSecondLicensePlate() != null) {
                                existingUser.setSecondLicensePlate(
                                        updatedUser.getSecondLicensePlate());
                            }

                            if (updatedUser.getPhoneNumber() != null) {
                                existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
                            }

                            if (updatedUser.getName() != null) {
                                existingUser.setName(updatedUser.getName());
                            }

                            if (updatedUser.getEmail() != null) {
                                existingUser.setEmail(updatedUser.getEmail());
                            }

                            if (updatedUser.getPassword() != null
                                    && !updatedUser.getPassword().isBlank()) {
                                existingUser.setPassword(
                                        passwordEncoder.encode(updatedUser.getPassword()));
                            }

                            if (updatedUser.getRole() != null) {
                                existingUser.setRole(updatedUser.getRole());
                            }

                            return userRepository.save(existingUser);
                        })
                .orElseThrow(
                        () ->
                                new RuntimeException(
                                        "User not found with id: " + updatedUser.getId()));
    }

    /**
     * Deletes a user by their ID.
     * @param id The ID of the user to delete
     */
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }
}
