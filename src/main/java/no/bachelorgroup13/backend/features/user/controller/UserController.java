package no.bachelorgroup13.backend.features.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import no.bachelorgroup13.backend.features.auth.security.CustomUserDetails;
import no.bachelorgroup13.backend.features.user.dto.UserDto;
import no.bachelorgroup13.backend.features.user.entity.User;
import no.bachelorgroup13.backend.features.user.mapper.UserMapper;
import no.bachelorgroup13.backend.features.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing user operations.
 * Provides endpoints for user CRUD operations and authentication.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "User", description = "Endpoints for managing users.")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieves all users in the system.
     * @return List of all users as DTOs
     */
    @Operation(summary = "Get all users")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(
                userService.getAllUsers().stream()
                        .map(userMapper::toDto)
                        .collect(Collectors.toList()));
    }

    /**
     * Retrieves a user by their ID.
     * @param id The user's ID
     * @return User DTO if found, 404 if not found
     */
    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        return userService
                .getUserById(id)
                .map(userMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a user by their license plate.
     * @param licensePlate The license plate to search for
     * @return User DTO if found, response with found=false if not found
     */
    @Operation(summary = "Get user by license plate")
    @GetMapping("/license-plate/{licensePlate}")
    public ResponseEntity<?> getUserByLicensePlate(@PathVariable String licensePlate) {
        System.out.println("License plate lookup request received for: " + licensePlate);

        Optional<User> userOptional = userService.getUserByLicensePlate(licensePlate);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            System.out.println("User found: " + user);
            return ResponseEntity.ok(userMapper.toDto(user));
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("found", String.valueOf(false));
            response.put("licensePlate", licensePlate);
            response.put("message", "No user found with license plate: ");
            System.out.println("No user found with license plate: " + licensePlate);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Creates a new user.
     * @param userDto The user data to create
     * @return Created user as DTO with status 201
     */
    @Operation(summary = "Create a new user")
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDto(createdUser));
    }

    /**
     * Updates an existing user.
     * Preserves existing values for null fields and handles password encryption.
     * @param id The ID of the user to update
     * @param userDto The updated user data
     * @return Updated user as DTO if found, 404 if not found
     */
    @Operation(summary = "Update user by ID")
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID id, @RequestBody UserDto userDto) {
        return userService
                .getUserById(id)
                .map(
                        existingUser -> {
                            User user = userMapper.toEntity(userDto);
                            user.setId(id);

                            if (user.getEmail() == null) {
                                user.setEmail(existingUser.getEmail());
                            }
                            if (user.getName() == null) {
                                user.setName(existingUser.getName());
                            }
                            if (user.getPassword() == null || user.getPassword().isBlank()) {
                                user.setPassword(existingUser.getPassword());
                            } else {
                                user.setPassword(passwordEncoder.encode(user.getPassword()));
                            }

                            user.setRole(existingUser.getRole());

                            User updatedUser = userService.updateUser(user);
                            return ResponseEntity.ok(userMapper.toDto(updatedUser));
                        })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a user by their ID.
     * @param id The ID of the user to delete
     * @return 204 No Content if successful, 404 if not found
     */
    @Operation(summary = "Delete user by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        return userService
                .getUserById(id)
                .map(
                        user -> {
                            userService.deleteUser(id);
                            return ResponseEntity.noContent().<Void>build();
                        })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves the currently authenticated user.
     * @param authentication The current authentication context
     * @return Current user as DTO if authenticated, 401 if not authenticated
     */
    @Operation(summary = "Get current user")
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null
                || !(authentication.getPrincipal()
                        instanceof CustomUserDetails customUserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return userService
                .getUserById(customUserDetails.getId())
                .map(user -> ResponseEntity.ok(userMapper.toDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }
}
