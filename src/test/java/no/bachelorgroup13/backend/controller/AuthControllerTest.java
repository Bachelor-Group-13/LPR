package no.bachelorgroup13.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import no.bachelorgroup13.backend.features.auth.controller.AuthController;
import no.bachelorgroup13.backend.features.auth.dto.SignupRequest;
import no.bachelorgroup13.backend.features.auth.security.JwtTokenProvider;
import no.bachelorgroup13.backend.features.auth.service.AuthService;
import no.bachelorgroup13.backend.features.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AuthService authService;

    @MockitoBean private UserRepository userRepository;

    @MockitoBean private JwtTokenProvider jwtTokenProvider;

    @MockitoBean private AuthenticationManager authenticationManager;

    @Test
    void testSignup_returnsCreated() throws Exception {
        when(authService.registerUser(any(SignupRequest.class))).thenReturn(null);

        String signupJson =
                """
                    {
                        "username": "testuser",
                        "email": "testuser@example.com",
                        "password": "password123",
                        "name": "Test User",
                        "phoneNumber": "12345678",
                        "licensePlate": "ABC123"
                    }
                """;

        mockMvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(signupJson))
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
}
