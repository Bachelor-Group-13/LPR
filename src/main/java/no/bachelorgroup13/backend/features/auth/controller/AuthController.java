package no.bachelorgroup13.backend.features.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.bachelorgroup13.backend.common.dto.MessageResponse;
import no.bachelorgroup13.backend.features.auth.dto.JwtResponse;
import no.bachelorgroup13.backend.features.auth.dto.LoginRequest;
import no.bachelorgroup13.backend.features.auth.dto.SignupRequest;
import no.bachelorgroup13.backend.features.auth.security.CustomUserDetails;
import no.bachelorgroup13.backend.features.auth.security.JwtTokenProvider;
import no.bachelorgroup13.backend.features.auth.service.AuthService;
import no.bachelorgroup13.backend.features.user.entity.User;
import no.bachelorgroup13.backend.features.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling user authentication operations.
 * Manages user login, registration, token refresh, and logout.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication and authorization endpoints")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Authenticates user and returns JWT token.
     * @param loginRequest User credentials
     * @param response HTTP response for setting cookies
     * @return JWT response with user details
     */
    @Operation(summary = "Login user")
    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenProvider.generateToken(authentication);

        Cookie jwtCookie = new Cookie("user", jwt);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(86400);
        jwtCookie.setSecure(false);
        response.addCookie(jwtCookie);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user =
                userRepository
                        .findByEmail(userDetails.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                new JwtResponse(
                        jwt,
                        "Bearer",
                        userDetails.getId(),
                        userDetails.getUsername(),
                        user.getName(),
                        null));
    }

    /**
     * Registers a new user in the system.
     * @param signUpRequest User registration details
     * @return Success message
     */
    @Operation(summary = "Register user")
    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> registerUser(
            @Valid @RequestBody SignupRequest signUpRequest) {
        return ResponseEntity.ok(authService.registerUser(signUpRequest));
    }

    /**
     * Refreshes the JWT token using refresh token.
     * @param request HTTP request containing refresh token cookie
     * @return New JWT response with tokens
     */
    @Operation(summary = "Check if user is logged in")
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(HttpServletRequest request) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String newAccessToken = authService.refreshToken(refreshToken);
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);

        return ResponseEntity.ok(
                new JwtResponse(
                        newAccessToken,
                        "Bearer",
                        user.getId(),
                        username,
                        user.getName(),
                        newRefreshToken));
    }

    /**
     * Logs out user by invalidating the JWT cookie.
     * @param response HTTP response for clearing cookies
     * @return Empty response
     */
    @Operation(summary = "Logout user")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("user", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        jwtCookie.setSecure(true);
        response.addCookie(jwtCookie);
        return ResponseEntity.ok().build();
    }
}
