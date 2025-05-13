package no.bachelorgroup13.backend.features.auth.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import no.bachelorgroup13.backend.common.dto.MessageResponse;
import no.bachelorgroup13.backend.features.auth.dto.JwtResponse;
import no.bachelorgroup13.backend.features.auth.dto.LoginRequest;
import no.bachelorgroup13.backend.features.auth.dto.SignupRequest;
import no.bachelorgroup13.backend.features.auth.security.JwtTokenProvider;
import no.bachelorgroup13.backend.features.user.entity.User;
import no.bachelorgroup13.backend.features.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwtTokenProvider;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        try {
            String rawEmail = loginRequest.getEmail().toLowerCase();
            User user =
                    userRepository
                            .findByEmail(rawEmail)
                            .orElseThrow(() -> new RuntimeException("User not found"));

            String encodedInput = encoder.encode(loginRequest.getPassword());
            log.info("Encoded input password: {}", encodedInput);

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    rawEmail, loginRequest.getPassword()));

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

    public MessageResponse registerUser(SignupRequest signUpRequest) {
        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            return new MessageResponse("Error: Email is already in use!");
        }

        // Create new user's account
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

    public String refreshToken(String refreshToken) {
        if (jwtTokenProvider.validateToken(refreshToken)) {
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            return jwtTokenProvider.generateTokenWithUsername(username);
        }
        throw new RuntimeException("Invalid refresh token");
    }
}
