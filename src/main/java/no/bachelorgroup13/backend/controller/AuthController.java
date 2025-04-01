package no.bachelorgroup13.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.bachelorgroup13.backend.dto.JwtResponse;
import no.bachelorgroup13.backend.dto.LoginRequest;
import no.bachelorgroup13.backend.dto.MessageResponse;
import no.bachelorgroup13.backend.dto.SignupRequest;
import no.bachelorgroup13.backend.service.AuthService;
import no.bachelorgroup13.backend.repository.UserRepository;
import no.bachelorgroup13.backend.security.JwtTokenProvider;
import no.bachelorgroup13.backend.entity.User;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

/* @CrossOrigin(origins = "*", maxAge = 3600) */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;

  @PostMapping("/check") public ResponseEntity<Boolean> checkPasswords(@RequestBody Map<String, String> passwords) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    return ResponseEntity.ok(encoder.matches(passwords.get("password"), passwords.get("hashedPassword")));
  }

  @PostMapping("/signin")
  public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    return ResponseEntity.ok(authService.authenticateUser(loginRequest));
  }

  @PostMapping("/signup")
  public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    return ResponseEntity.ok(authService.registerUser(signUpRequest));
  }

  @PostMapping("/refresh")
  public ResponseEntity<JwtResponse> refreshToken(@RequestBody String refreshToken) {
    String newAccessToken = authService.refreshToken(refreshToken);
    String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
    User user = userRepository.findByEmail(username)
        .orElseThrow(() -> new RuntimeException("User not found"));

    String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);

    return ResponseEntity
        .ok(new JwtResponse(newAccessToken, "Bearer", user.getId(), user.getEmail(), user.getName(), newRefreshToken));
  }
}
