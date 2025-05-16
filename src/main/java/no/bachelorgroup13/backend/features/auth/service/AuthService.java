package no.bachelorgroup13.backend.features.auth.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import no.bachelorgroup13.backend.common.dto.MessageResponse;
import no.bachelorgroup13.backend.features.auth.dto.JwtResponse;
import no.bachelorgroup13.backend.features.auth.dto.LoginRequest;
import no.bachelorgroup13.backend.features.auth.dto.SignupRequest;
import no.bachelorgroup13.backend.features.auth.security.JwtTokenProvider;
import no.bachelorgroup13.backend.features.user.entity.User;
import no.bachelorgroup13.backend.features.user.repository.UserRepository;

/**
 * Service handling user authentication and registration.
 * Manages user login, signup, and token refresh operations.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwtTokenProvider;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /**
     * Authenticates a user and generates JWT tokens.
     * @param loginRequest User login credentials
     * @return JWT response containing access and refresh tokens
     */
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        try {
            User user =
                    userRepository
                            .findByEmail(loginRequest.getEmail())
                            .orElseThrow(() -> new RuntimeException("User not found"));

            String encodedInput = encoder.encode(loginRequest.getPassword());
            log.info("Encoded input password: {}", encodedInput);

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(loginRequest.getEmail());

            return new JwtResponse(
                    jwt, "Bearer", user.getId(), user.getEmail(), user.getName(), refreshToken);
        } catch (AuthenticationException e) {
            log.error("Authentication failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Registers a new user in the system.
     * @param signUpRequest User registration details
     * @return Success or error message
     */
    public MessageResponse registerUser(SignupRequest signUpRequest) {
        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            return new MessageResponse("Error: Email is already in use!");
        }

        // Create a new user's account
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(signUpRequest.getEmail().toLowerCase());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setName(signUpRequest.getName());
        user.setLicensePlate(
                signUpRequest.getLicensePlate() != null
                        ? signUpRequest.getLicensePlate().toUpperCase()
                        : null);
        user.setPhoneNumber(signUpRequest.getPhoneNumber());
        user.setEnabled(true);

        userRepository.save(user);

        return new MessageResponse("User registered successfully!");
    }

    /**
     * Refreshes the JWT token using a valid refresh token.
     * @param refreshToken Current refresh token
     * @return New JWT token
     */
    public String refreshToken(String refreshToken) {
        if (jwtTokenProvider.validateToken(refreshToken)) {
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            return jwtTokenProvider.generateTokenWithUsername(username);
        }
        throw new RuntimeException("Invalid refresh token");
    }
}
