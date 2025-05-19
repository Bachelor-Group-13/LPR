package no.bachelorgroup13.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import no.bachelorgroup13.backend.features.auth.security.CustomUserDetails;
import no.bachelorgroup13.backend.features.auth.security.JwtAuthenticationFilter;
import no.bachelorgroup13.backend.features.auth.security.JwtTokenProvider;
import no.bachelorgroup13.backend.features.auth.security.Role;
import no.bachelorgroup13.backend.features.user.controller.UserController;
import no.bachelorgroup13.backend.features.user.dto.UserDto;
import no.bachelorgroup13.backend.features.user.entity.User;
import no.bachelorgroup13.backend.features.user.mapper.UserMapper;
import no.bachelorgroup13.backend.features.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private UserService userService;

    @MockitoBean private UserMapper userMapper;

    @MockitoBean private JwtTokenProvider jwtTokenProvider;

    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean private PasswordEncoder passwordEncoder;

    private User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("testuser@example.com");
        user.setPassword("password123");
        user.setName("Test User");
        user.setPhoneNumber("12345678");
        user.setLicensePlate("ABC123");
        user.setEnabled(true);
        user.setRole(Role.ROLE_USER);
        return user;
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetUserById() throws Exception {
        User testUser = createTestUser();
        UUID userId = testUser.getId();

        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setEmail(testUser.getEmail());
        userDto.setName(testUser.getName());
        userDto.setPhoneNumber(testUser.getPhoneNumber());
        userDto.setLicensePlate(testUser.getLicensePlate());

        CustomUserDetails customUserDetails =
                new CustomUserDetails(
                        testUser.getId(),
                        testUser.getEmail(),
                        testUser.getPassword(),
                        true,
                        testUser.getRole());

        when(userService.getUserById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        when(jwtTokenProvider.validateToken(any())).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(any())).thenReturn(testUser.getEmail());
        when(jwtTokenProvider.getAuthentication(any()))
                .thenReturn(
                        new UsernamePasswordAuthenticationToken(
                                customUserDetails,
                                null,
                                Collections.singletonList(
                                        new SimpleGrantedAuthority(testUser.getRole().name()))));

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/auth/{id}", userId)
                                .cookie(new Cookie("user", "test-token")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(userId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(testUser.getName()))
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.phoneNumber")
                                .value(testUser.getPhoneNumber()))
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.licensePlate")
                                .value(testUser.getLicensePlate()));
    }
}
