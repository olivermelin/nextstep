package se.sobriety.nextstep.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.sobriety.nextstep.dto.UserSettingsInDto;
import se.sobriety.nextstep.dto.UserSettingsOutDto;
import se.sobriety.nextstep.entity.UserSettings;
import se.sobriety.nextstep.exception.UserNotFoundException;
import se.sobriety.nextstep.mapper.UserSettingsMapper;
import se.sobriety.nextstep.repository.UserSettingsRepository;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserSettingsServiceTest {

    @Mock
    private UserSettingsRepository userSettingsRepository;
    @Mock
    private UserSettingsMapper userSettingsMapper;

    private UserSettingsService userSettingsService;

    private static final String USER_ID = "user-123";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userSettingsService = new UserSettingsService(userSettingsRepository, userSettingsMapper);
    }

    // ==================== getUserSettings ====================

    @Test
    void getUserSettings_existingUser_returnsDto() {
        UserSettings entity = new UserSettings(USER_ID);
        UserSettingsOutDto expectedDto = buildOutDto(USER_ID, "Default Name", "", "", true, false, false, "en");

        when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.of(entity));
        when(userSettingsMapper.toDto(entity)).thenReturn(expectedDto);

        UserSettingsOutDto result = userSettingsService.getUserSettings(USER_ID);

        assertEquals(expectedDto, result);
        verify(userSettingsRepository).findByUserId(USER_ID);
        verify(userSettingsRepository, never()).save(any());
    }

    @Test
    void getUserSettings_nonExistingUser_createsDefaultAndReturnsDto() {
        UserSettings newEntity = new UserSettings(USER_ID);
        UserSettingsOutDto expectedDto = buildOutDto(USER_ID, "Default Name", "", "", true, false, false, "en");

        when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userSettingsRepository.save(any(UserSettings.class))).thenReturn(newEntity);
        when(userSettingsMapper.toDto(newEntity)).thenReturn(expectedDto);

        UserSettingsOutDto result = userSettingsService.getUserSettings(USER_ID);

        assertEquals(expectedDto, result);
        verify(userSettingsRepository).save(any(UserSettings.class));
        verify(userSettingsMapper).toDto(newEntity);
    }

    // ==================== updateUserSettings ====================

    @Test
    void updateUserSettings_existingUser_updatesAndReturnsDto() {
        UserSettings entity = new UserSettings(USER_ID);
        UserSettingsInDto inDto = new UserSettingsInDto(USER_ID, "New Name", "new@example.com", "+123", false, true, true, "sv", null, null, null);
        UserSettingsOutDto expectedDto = buildOutDto(USER_ID, "New Name", "new@example.com", "+123", false, true, true, "sv");

        when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.of(entity));
        when(userSettingsMapper.toDto(entity)).thenReturn(expectedDto);

        UserSettingsOutDto result = userSettingsService.updateUserSettings(USER_ID, inDto);

        assertEquals(expectedDto, result);
        assertEquals("New Name", entity.getName());
        assertEquals("new@example.com", entity.getEmail());
        assertEquals("+123", entity.getPhone());
        assertFalse(entity.isNotificationsEnabled());
        assertTrue(entity.isAiNotificationsEnabled());
        assertTrue(entity.isDarkModeEnabled());
        assertEquals("sv", entity.getLanguage());
    }

    @Test
    void updateUserSettings_nonExistingUser_createsDefaultThenUpdates() {
        UserSettings newEntity = new UserSettings(USER_ID);
        UserSettingsInDto inDto = new UserSettingsInDto(USER_ID, "Name", "a@b.com", "555", true, true, false, "en", null, null, null);
        UserSettingsOutDto expectedDto = buildOutDto(USER_ID, "Name", "a@b.com", "555", true, true, false, "en");

        when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userSettingsRepository.save(any(UserSettings.class))).thenReturn(newEntity);
        when(userSettingsMapper.toDto(newEntity)).thenReturn(expectedDto);

        UserSettingsOutDto result = userSettingsService.updateUserSettings(USER_ID, inDto);

        assertEquals(expectedDto, result);
        // 2 saves förväntas: createAndSaveDefault + explicit save efter applyUpdates
        verify(userSettingsRepository, times(2)).save(any(UserSettings.class));
    }

    @Test
    void updateUserSettings_invalidEmail_throwsException() {
        UserSettingsInDto inDto = new UserSettingsInDto(USER_ID, "Name", "not-an-email", "555", true, false, false, "en", null, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> userSettingsService.updateUserSettings(USER_ID, inDto));

        verify(userSettingsRepository, never()).findByUserId(any());
    }

    @Test
    void updateUserSettings_nullEmail_doesNotThrow() {
        UserSettings entity = new UserSettings(USER_ID);
        UserSettingsInDto inDto = new UserSettingsInDto(USER_ID, "Name", null, "555", true, false, false, "en", null, null, null);
        UserSettingsOutDto expectedDto = buildOutDto(USER_ID, "Name", null, "555", true, false, false, "en");

        when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.of(entity));
        when(userSettingsMapper.toDto(entity)).thenReturn(expectedDto);

        UserSettingsOutDto result = userSettingsService.updateUserSettings(USER_ID, inDto);

        assertNotNull(result);
    }

    @Test
    void updateUserSettings_blankEmail_doesNotThrow() {
        UserSettings entity = new UserSettings(USER_ID);
        UserSettingsInDto inDto = new UserSettingsInDto(USER_ID, "Name", "   ", "555", true, false, false, "en", null, null, null);
        UserSettingsOutDto expectedDto = buildOutDto(USER_ID, "Name", "   ", "555", true, false, false, "en");

        when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.of(entity));
        when(userSettingsMapper.toDto(entity)).thenReturn(expectedDto);

        UserSettingsOutDto result = userSettingsService.updateUserSettings(USER_ID, inDto);

        assertNotNull(result);
    }

    @Test
    void updateUserSettings_emptyStringEmail_doesNotThrow() {
        UserSettings entity = new UserSettings(USER_ID);
        UserSettingsInDto inDto = new UserSettingsInDto(USER_ID, "Name", "", "555", true, false, false, "en", null, null, null);
        UserSettingsOutDto expectedDto = buildOutDto(USER_ID, "Name", "", "555", true, false, false, "en");

        when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.of(entity));
        when(userSettingsMapper.toDto(entity)).thenReturn(expectedDto);

        UserSettingsOutDto result = userSettingsService.updateUserSettings(USER_ID, inDto);

        assertNotNull(result);
    }

    @Test
    void updateUserSettings_validEmailWithAt_doesNotThrow() {
        UserSettings entity = new UserSettings(USER_ID);
        UserSettingsInDto inDto = new UserSettingsInDto(USER_ID, "Name", "user@domain.com", "555", true, false, false, "en", null, null, null);
        UserSettingsOutDto expectedDto = buildOutDto(USER_ID, "Name", "user@domain.com", "555", true, false, false, "en");

        when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.of(entity));
        when(userSettingsMapper.toDto(entity)).thenReturn(expectedDto);

        UserSettingsOutDto result = userSettingsService.updateUserSettings(USER_ID, inDto);

        assertNotNull(result);
    }

    // ==================== deleteUserSettings ====================

    @Test
    void deleteUserSettings_existingUser_deletesEntity() {
        UserSettings entity = new UserSettings(USER_ID);
        when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.of(entity));

        userSettingsService.deleteUserSettings(USER_ID);

        verify(userSettingsRepository).delete(entity);
    }

    @Test
    void deleteUserSettings_nonExistingUser_throwsUserNotFoundException() {
        when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userSettingsService.deleteUserSettings(USER_ID));

        verify(userSettingsRepository, never()).delete(any());
    }

    // ==================== getOrCreateSettings ====================

    @Test
    void getOrCreateSettings_existingUser_returnsExistingEntity() {
        UserSettings entity = new UserSettings(USER_ID);
        when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.of(entity));

        UserSettings result = userSettingsService.getOrCreateSettings(USER_ID);

        assertSame(entity, result);
        verify(userSettingsRepository, never()).save(any());
    }

    @Test
    void getOrCreateSettings_nonExistingUser_createsAndReturnsNewEntity() {
        UserSettings newEntity = new UserSettings(USER_ID);
        when(userSettingsRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userSettingsRepository.save(any(UserSettings.class))).thenReturn(newEntity);

        UserSettings result = userSettingsService.getOrCreateSettings(USER_ID);

        assertSame(newEntity, result);
        verify(userSettingsRepository).save(any(UserSettings.class));
    }

    // ==================== helpers ====================

    private UserSettingsOutDto buildOutDto(String userId, String name, String email, String phone,
                                           boolean notifications, boolean aiNotifications,
                                           boolean darkMode, String language) {
        return new UserSettingsOutDto(userId, name, email, phone, notifications, aiNotifications,
                darkMode, language, false, null, null, null, null, null,
                se.sobriety.nextstep.entity.SubscriptionTier.FREE,
                se.sobriety.nextstep.entity.CoachPersonality.SUPPORTIVE,
                false, Set.of());
    }
}
