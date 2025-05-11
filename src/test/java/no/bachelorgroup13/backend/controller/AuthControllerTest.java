package no.bachelorgroup13.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import no.bachelorgroup13.backend.features.auth.controller.AuthController;
import no.bachelorgroup13.backend.features.auth.dto.JwtResponse;
import no.bachelorgroup13.backend.features.auth.dto.LoginRequest;
import no.bachelorgroup13.backend.features.auth.dto.SignupRequest;
import no.bachelorgroup13.backend.features.auth.security.CustomUserDetails;
import no.bachelorgroup13.backend.features.auth.security.JwtTokenProvider;
import no.bachelorgroup13.backend.features.auth.security.Role;
import no.bachelorgroup13.backend.features.auth.service.AuthService;
import no.bachelorgroup13.backend.features.user.entity.User;
import no.bachelorgroup13.backend.features.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private AuthService authService;

    @MockitoBean private UserRepository userRepository;

    @MockitoBean private JwtTokenProvider jwtTokenProvider;

    @MockitoBean private AuthenticationManager authenticationManager;

    private User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("testuser@example.com");
        user.setPassword("password123");
        user.setName("Test User");
        user.setPhoneNumber("12345678");
        user.setLicensePlate("ABC123");
        user.setEnabled(true);
        return user;
    }

    @Test
    void testSignup_returnsCreated() throws Exception {
        User testUser = createTestUser();
        when(authService.registerUser(any(SignupRequest.class))).thenReturn(null);

        SignupRequest signupJson = new SignupRequest();
        signupJson.setEmail(testUser.getEmail());
        signupJson.setPassword(testUser.getPassword());
        signupJson.setName(testUser.getName());
        signupJson.setPhoneNumber(testUser.getPhoneNumber());
        signupJson.setLicensePlate(testUser.getLicensePlate());

        mockMvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupJson)))
                .andExpect(status().isOk());
    }

    @Test
    void testSignup_returnsBadRequest() throws Exception {
        when(authService.registerUser(any(SignupRequest.class)))
                .thenThrow(new RuntimeException("User already exists"));

        String signupJson2 =
                """
                    {
                        "username": "testuser",
                        "email": ""
                    }
                """;

        mockMvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(signupJson2))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignupAndLogin() throws Exception {
        // First signup
        User testUser = createTestUser();
        when(authService.registerUser(any(SignupRequest.class))).thenReturn(null);

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail(testUser.getEmail());
        signupRequest.setPassword(testUser.getPassword());
        signupRequest.setName(testUser.getName());
        signupRequest.setPhoneNumber(testUser.getPhoneNumber());
        signupRequest.setLicensePlate(testUser.getLicensePlate());

        mockMvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // Then login with the same user
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(testUser.getEmail());
        loginRequest.setPassword(testUser.getPassword());

        JwtResponse jwtResponse =
                new JwtResponse(
                        "dummyToken",
                        "Bearer",
                        testUser.getId(),
                        testUser.getEmail(),
                        testUser.getName(),
                        "dummyRefreshToken");

        CustomUserDetails userDetails =
                new CustomUserDetails(
                        testUser.getId(),
                        testUser.getEmail(),
                        testUser.getPassword(),
                        true,
                        Role.ROLE_USER);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails, testUser.getPassword(), userDetails.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(jwtResponse);

        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("dummyToken");

        mockMvc.perform(
                        post("/api/auth/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("dummyToken"))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.name").value(testUser.getName()));
    }
}
