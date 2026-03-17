package se.sobriety.nextstep.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.sobriety.nextstep.dto.BackgroundInfoDto;
import se.sobriety.nextstep.dto.OnboardingDataDto;
import se.sobriety.nextstep.entity.*;
import se.sobriety.nextstep.repository.UserSettingsRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OnboardingServiceTest {

    @Mock
    private UserSettingsRepository userSettingsRepository;
    @Mock
    private UserSettingsService userSettingsService;

    private OnboardingService onboardingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        onboardingService = new OnboardingService(userSettingsRepository, userSettingsService);
    }

    // ============ completeOnboarding: success ============

    @Test
    void completeOnboarding_validData_savesSuccessfully() {
        String userId = "user-123";
        OnboardingDataDto data = new OnboardingDataDto(
                List.of(UserGoal.DAILY_STABILITY),
                null,
                null,
                RecoveryStage.TRYING_TO_QUIT
        );

        UserSettings settings = new UserSettings(userId);
        when(userSettingsService.getOrCreateSettings(userId)).thenReturn(settings);
        when(userSettingsRepository.save(any(UserSettings.class))).thenReturn(settings);

        onboardingService.completeOnboarding(userId, data);

        verify(userSettingsRepository).save(any(UserSettings.class));
        assertTrue(settings.getOnboardingCompleted());
        assertEquals(List.of(UserGoal.DAILY_STABILITY), settings.getUserGoals());
        assertEquals(RecoveryStage.TRYING_TO_QUIT, settings.getRecoveryStage());
    }

    @Test
    void completeOnboarding_withBackgroundInfo_savesBackgroundInfo() {
        String userId = "user-456";
        BackgroundInfoDto bgDto = new BackgroundInfoDto(
                25, Gender.MALE, CurrentSituation.IN_TREATMENT, List.of(SubstanceType.ALCOHOL)
        );
        OnboardingDataDto data = new OnboardingDataDto(
                List.of(UserGoal.REDUCE_SUBSTANCES, UserGoal.MENTAL_HEALTH),
                "My custom goal",
                bgDto,
                RecoveryStage.ONE_TO_FOUR_WEEKS
        );

        UserSettings settings = new UserSettings(userId);
        when(userSettingsService.getOrCreateSettings(userId)).thenReturn(settings);
        when(userSettingsRepository.save(any(UserSettings.class))).thenReturn(settings);

        onboardingService.completeOnboarding(userId, data);

        verify(userSettingsRepository).save(any(UserSettings.class));
        assertTrue(settings.getOnboardingCompleted());
        assertNotNull(settings.getBackgroundInfo());
        assertEquals(25, settings.getBackgroundInfo().getAge());
        assertEquals("My custom goal", settings.getOtherGoal());
    }

    // ============ completeOnboarding: validation failures ============

    @Test
    void completeOnboarding_nullUserId_throwsException() {
        OnboardingDataDto data = new OnboardingDataDto(
                List.of(UserGoal.DAILY_STABILITY), null, null, RecoveryStage.ACTIVE_USE
        );

        assertThrows(IllegalArgumentException.class,
                () -> onboardingService.completeOnboarding(null, data));
        verify(userSettingsService, never()).getOrCreateSettings(any());
    }

    @Test
    void completeOnboarding_blankUserId_throwsException() {
        OnboardingDataDto data = new OnboardingDataDto(
                List.of(UserGoal.DAILY_STABILITY), null, null, RecoveryStage.ACTIVE_USE
        );

        assertThrows(IllegalArgumentException.class,
                () -> onboardingService.completeOnboarding("   ", data));
        verify(userSettingsService, never()).getOrCreateSettings(any());
    }

    @Test
    void completeOnboarding_nullData_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> onboardingService.completeOnboarding("user-123", null));
        verify(userSettingsService, never()).getOrCreateSettings(any());
    }

    @Test
    void completeOnboarding_nullUserGoals_throwsException() {
        OnboardingDataDto data = new OnboardingDataDto(
                null, null, null, RecoveryStage.ACTIVE_USE
        );

        assertThrows(IllegalArgumentException.class,
                () -> onboardingService.completeOnboarding("user-123", data));
    }

    @Test
    void completeOnboarding_emptyUserGoals_throwsException() {
        OnboardingDataDto data = new OnboardingDataDto(
                Collections.emptyList(), null, null, RecoveryStage.ACTIVE_USE
        );

        assertThrows(IllegalArgumentException.class,
                () -> onboardingService.completeOnboarding("user-123", data));
    }

    @Test
    void completeOnboarding_nullRecoveryStage_succeeds() {
        OnboardingDataDto data = new OnboardingDataDto(
                List.of(UserGoal.DAILY_STABILITY), null, null, null
        );

        UserSettings settings = new UserSettings("user-123");
        when(userSettingsService.getOrCreateSettings("user-123")).thenReturn(settings);
        when(userSettingsRepository.save(any(UserSettings.class))).thenReturn(settings);

        assertDoesNotThrow(() -> onboardingService.completeOnboarding("user-123", data));
        verify(userSettingsRepository).save(any(UserSettings.class));
        assertNull(settings.getRecoveryStage());
    }

    @Test
    void completeOnboarding_otherGoalTooLong_throwsException() {
        String tooLong = "x".repeat(1001);
        OnboardingDataDto data = new OnboardingDataDto(
                List.of(UserGoal.OTHER), tooLong, null, RecoveryStage.ACTIVE_USE
        );

        assertThrows(IllegalArgumentException.class,
                () -> onboardingService.completeOnboarding("user-123", data));
    }

    @Test
    void completeOnboarding_otherGoalAtLimit_succeeds() {
        String atLimit = "x".repeat(1000);
        OnboardingDataDto data = new OnboardingDataDto(
                List.of(UserGoal.OTHER), atLimit, null, RecoveryStage.ACTIVE_USE
        );

        UserSettings settings = new UserSettings("user-123");
        when(userSettingsService.getOrCreateSettings("user-123")).thenReturn(settings);
        when(userSettingsRepository.save(any(UserSettings.class))).thenReturn(settings);

        assertDoesNotThrow(() -> onboardingService.completeOnboarding("user-123", data));
        verify(userSettingsRepository).save(any(UserSettings.class));
    }

    // ============ isOnboardingCompleted ============

    @Test
    void isOnboardingCompleted_userHasCompletedByUserId_returnsTrue() {
        UserSettings settings = new UserSettings("user-123");
        settings.setOnboardingCompleted(true);
        when(userSettingsRepository.findAllByUserId("user-123")).thenReturn(List.of(settings));

        assertTrue(onboardingService.isOnboardingCompleted("user-123"));
    }

    @Test
    void isOnboardingCompleted_userHasNotCompleted_returnsFalse() {
        UserSettings settings = new UserSettings("user-123");
        settings.setOnboardingCompleted(false);
        when(userSettingsRepository.findAllByUserId("user-123")).thenReturn(List.of(settings));
        when(userSettingsRepository.findAllByEmail("user-123")).thenReturn(Collections.emptyList());

        assertFalse(onboardingService.isOnboardingCompleted("user-123"));
    }

    @Test
    void isOnboardingCompleted_noSettingsExist_returnsFalse() {
        when(userSettingsRepository.findAllByUserId("user-123")).thenReturn(Collections.emptyList());
        when(userSettingsRepository.findAllByEmail("user-123")).thenReturn(Collections.emptyList());

        assertFalse(onboardingService.isOnboardingCompleted("user-123"));
    }

    @Test
    void isOnboardingCompleted_foundByEmailFallback_returnsTrue() {
        when(userSettingsRepository.findAllByUserId("user@example.com")).thenReturn(Collections.emptyList());

        UserSettings settings = new UserSettings("old-sub-id");
        settings.setOnboardingCompleted(true);
        when(userSettingsRepository.findAllByEmail("user@example.com")).thenReturn(List.of(settings));

        assertTrue(onboardingService.isOnboardingCompleted("user@example.com"));
    }

    @Test
    void isOnboardingCompleted_nullUserId_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> onboardingService.isOnboardingCompleted(null));
    }

    @Test
    void isOnboardingCompleted_blankUserId_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> onboardingService.isOnboardingCompleted("  "));
    }
}
