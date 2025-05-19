package no.bachelorgroup13.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.UUID;
import no.bachelorgroup13.backend.features.auth.security.JwtTokenProvider;
import no.bachelorgroup13.backend.features.push.repository.PushSubscriptionRepository;
import no.bachelorgroup13.backend.features.push.service.WebPushService;
import no.bachelorgroup13.backend.features.reservation.controller.ReservationController;
import no.bachelorgroup13.backend.features.reservation.dto.ReservationDto;
import no.bachelorgroup13.backend.features.reservation.entity.Reservation;
import no.bachelorgroup13.backend.features.reservation.mapper.ReservationMapper;
import no.bachelorgroup13.backend.features.reservation.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReservationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;
    @MockitoBean
    private ReservationMapper reservationMapper;
    @MockitoBean
    private PushSubscriptionRepository pushRepository;
    @MockitoBean
    private WebPushService pushService;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Test
    void testCreateReservation_returnsCreated() throws Exception {
        UUID userId = UUID.randomUUID();
        ReservationDto dto = new ReservationDto();
        dto.setSpotNumber("1A");
        dto.setReservationDate(LocalDate.now());
        dto.setUserId(userId);
        dto.setLicensePlate("TE123456");
        dto.setAnonymous(false);

        Reservation reservation = new Reservation();
        reservation.setSpotNumber(dto.getSpotNumber());
        reservation.setUserId(userId);
        reservation.setReservationDate(dto.getReservationDate());
        reservation.setLicensePlate(dto.getLicensePlate());
        reservation.setAnonymous(dto.isAnonymous());

        when(reservationMapper.toEntity(any())).thenReturn(reservation);
        when(reservationService.createReservation(any())).thenReturn(reservation);
        when(reservationMapper.toDto(any())).thenReturn(dto);
        when(reservationService.hasActiveReservation(any())).thenReturn(false);

        mockMvc.perform(
                post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }
}
