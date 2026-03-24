package se.sobriety.nextstep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.sobriety.nextstep.dto.UserSettingsInDto;
import se.sobriety.nextstep.dto.UserSettingsOutDto;
import se.sobriety.nextstep.exception.GlobalExceptionHandler;
import se.sobriety.nextstep.service.UserSettingsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserSettingsControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserSettingsService userSettingsService;

    private static final String USER_ID = "test@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        UserSettingsController controller = new UserSettingsController(userSettingsService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(USER_ID, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUserSettings_returns200() throws Exception {
        UserSettingsOutDto out = new UserSettingsOutDto(
                USER_ID, "Test User", "test@example.com", "1234567890",
                true, false, false, "en",
                true, List.of(), null, null, null,
                LocalDateTime.of(2025, 1, 1, 12, 0));

        when(userSettingsService.getUserSettings(USER_ID)).thenReturn(out);

        mockMvc.perform(get("/api/settings/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.notificationsEnabled").value(true));

        verify(userSettingsService).getUserSettings(USER_ID);
    }

    @Test
    void updateUserSettings_validData_returns200() throws Exception {
        UserSettingsInDto inDto = new UserSettingsInDto(
                USER_ID, "Updated Name", "test@example.com", "0987654321",
                true, true, true, "sv");

        UserSettingsOutDto out = new UserSettingsOutDto(
                USER_ID, "Updated Name", "test@example.com", "0987654321",
                true, true, true, "sv",
                true, List.of(), null, null, null,
                LocalDateTime.of(2025, 1, 1, 12, 0));

        when(userSettingsService.updateUserSettings(eq(USER_ID), any(UserSettingsInDto.class)))
                .thenReturn(out);

        mockMvc.perform(post("/api/settings/{userId}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.darkModeEnabled").value(true))
                .andExpect(jsonPath("$.language").value("sv"));

        verify(userSettingsService).updateUserSettings(eq(USER_ID), any(UserSettingsInDto.class));
    }

    @Test
    void updateUserSettings_invalidEmail_returns400() throws Exception {
        UserSettingsInDto inDto = new UserSettingsInDto(
                USER_ID, "Test User", "not-an-email", "1234567890",
                true, false, false, "en");

        mockMvc.perform(post("/api/settings/{userId}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUserSettings_returns204() throws Exception {
        mockMvc.perform(delete("/api/settings/{userId}", USER_ID))
                .andExpect(status().isNoContent());

        verify(userSettingsService).deleteUserSettings(USER_ID);
    }

    @Test
    void getUserSettings_wrongUser_returns403() throws Exception {
        mockMvc.perform(get("/api/settings/{userId}", "other@example.com"))
                .andExpect(status().isForbidden());
    }
}
