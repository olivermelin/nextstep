package se.sobriety.nextstep.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.sobriety.nextstep.dto.ProgressResponseDto;
import se.sobriety.nextstep.entity.*;
import se.sobriety.nextstep.exception.UserNotFoundException;
import se.sobriety.nextstep.repository.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserProgressServiceTest {

    @Mock private UserProgressRepository progressRepository;
    @Mock private UserSettingsRepository settingsRepository;
    @Mock private LevelService levelService;
    @Mock private MailService mailService;
    @Mock private CategoryProgressRepository categoryProgressRepository;
    @Mock private UserChallengeRepository userChallengeRepository;
    @Mock private AchievementRepository achievementRepository;
    @Mock private UserAchievementRepository userAchievementRepository;
    @Mock private ActivityFeedService activityFeedService;

    private UserProgressService service;

    private static final String USER_ID = "user@test.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UserProgressService(
                progressRepository, settingsRepository, levelService, mailService,
                categoryProgressRepository, userChallengeRepository,
                achievementRepository, userAchievementRepository, activityFeedService
        );
    }

    // --- getUser ---

    @Test
    void getUser_existing_returnsUser() {
        UserProgress progress = new UserProgress(USER_ID);
        when(progressRepository.findByUserId(USER_ID)).thenReturn(Optional.of(progress));

        UserProgress result = service.getUser(USER_ID);
        assertEquals(USER_ID, result.getUserId());
    }

    @Test
    void getUser_nonexistent_throwsException() {
        when(progressRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.getUser(USER_ID));
    }

    // --- getOrCreateUser ---

    @Test
    void getOrCreateUser_existing_returnsExisting() {
        UserProgress progress = new UserProgress(USER_ID);
        when(progressRepository.findByUserId(USER_ID)).thenReturn(Optional.of(progress));

        UserProgress result = service.getOrCreateUser(USER_ID);

        assertEquals(USER_ID, result.getUserId());
        verify(progressRepository, never()).save(any());
    }

    @Test
    void getOrCreateUser_new_createsAndReturns() {
        when(progressRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(progressRepository.save(any(UserProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProgress result = service.getOrCreateUser(USER_ID);

        assertEquals(USER_ID, result.getUserId());
        verify(progressRepository).save(any(UserProgress.class));
    }

    // --- createUser ---

    @Test
    void createUser_validId_createsUser() {
        when(progressRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserProgress result = service.createUser(USER_ID);

        assertEquals(USER_ID, result.getUserId());
        assertEquals(0, result.getTotalPoints());
        assertEquals(1, result.getLevel());
    }

    @Test
    void createUser_nullId_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> service.createUser(null));
    }

    @Test
    void createUser_blankId_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> service.createUser("  "));
    }

    @Test
    void createUser_duplicate_throwsException() {
        when(progressRepository.findByUserId(USER_ID)).thenReturn(Optional.of(new UserProgress(USER_ID)));

        assertThrows(IllegalArgumentException.class, () -> service.createUser(USER_ID));
    }

    // --- addPoints ---

    @Test
    void addPoints_addsAndRecalculatesLevel() {
        UserProgress progress = new UserProgress(USER_ID);
        when(progressRepository.findByUserId(USER_ID)).thenReturn(Optional.of(progress));
        when(levelService.calculateLevel(10)).thenReturn(1);
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserProgress result = service.addPoints(USER_ID, 10);

        assertEquals(10, result.getTotalPoints());
        verify(levelService).calculateLevel(10);
    }

    @Test
    void addPoints_levelUp_sendsEmail() {
        UserProgress progress = new UserProgress(USER_ID);
        // Simulate level 1 -> level 2
        when(progressRepository.findByUserId(USER_ID)).thenReturn(Optional.of(progress));
        when(levelService.calculateLevel(anyLong())).thenReturn(2);
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserSettings settings = mock(UserSettings.class);
        when(settings.getEmail()).thenReturn("user@test.com");
        when(settings.getName()).thenReturn("Test User");
        when(settingsRepository.findByUserId(USER_ID)).thenReturn(Optional.of(settings));

        service.addPoints(USER_ID, 100);

        verify(mailService).sendMail(any());
    }

    @Test
    void addPoints_noLevelUp_doesNotSendEmail() {
        UserProgress progress = new UserProgress(USER_ID);
        when(progressRepository.findByUserId(USER_ID)).thenReturn(Optional.of(progress));
        when(levelService.calculateLevel(anyLong())).thenReturn(1); // stays level 1
        when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.addPoints(USER_ID, 10);

        verify(mailService, never()).sendMail(any());
    }

    // --- getTotalProgress ---

    @Test
    void getTotalProgress_returnsCorrectDto() {
        UserProgress progress = new UserProgress(USER_ID);
        progress.applyPoints(50);
        progress.updateLevel(2);
        when(progressRepository.findByUserId(USER_ID)).thenReturn(Optional.of(progress));

        ProgressResponseDto result = service.getTotalProgress(USER_ID);

        assertEquals(USER_ID, result.userId());
        assertEquals(50, result.totalPoints());
        assertEquals(2, result.level());
    }

    // --- getUserAchievements ---

    @Test
    void getUserAchievements_unlocksBasedOnPointsAndLevel() {
        UserProgress progress = new UserProgress(USER_ID);
        progress.applyPoints(200);
        progress.updateLevel(3);
        when(progressRepository.findByUserId(USER_ID)).thenReturn(Optional.of(progress));

        Achievement easy = new Achievement(1, "Beginner", "Start your journey", 0, 1);
        Achievement hard = new Achievement(2, "Advanced", "Reach level 10", 1000, 10);

        when(achievementRepository.findAll()).thenReturn(List.of(easy, hard));

        var achievements = service.getUserAchievements(USER_ID);

        assertEquals(2, achievements.size());
        assertTrue(achievements.get(0).unlocked());   // easy: 200 >= 0 && 3 >= 1
        assertFalse(achievements.get(1).unlocked());   // hard: 200 < 1000
    }
}
