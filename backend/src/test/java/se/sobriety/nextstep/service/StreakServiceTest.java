package se.sobriety.nextstep.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.sobriety.nextstep.dto.StreakResponseDto;
import se.sobriety.nextstep.entity.UserStreak;
import se.sobriety.nextstep.repository.UserStreakRepository;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StreakServiceTest {

    @Mock private UserStreakRepository streakRepository;
    @Mock private ActivityFeedService activityFeedService;

    private StreakService service;

    private static final String USER_ID = "user@test.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new StreakService(streakRepository, activityFeedService);
    }

    // --- getStreak ---

    @Test
    void getStreak_noExisting_returnsEmptyDefault() {
        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        StreakResponseDto result = service.getStreak(USER_ID);

        assertEquals(USER_ID, result.userId());
        assertEquals(0, result.currentStreak());
        assertEquals(0, result.longestStreak());
        assertFalse(result.streakActive());
        assertFalse(result.activityRegisteredToday());
    }

    @Test
    void getStreak_existing_returnsCorrectDto() throws Exception {
        UserStreak streak = new UserStreak(USER_ID);
        setField(streak, "currentStreak", 5);
        setField(streak, "longestStreak", 10);
        setField(streak, "lastActivityDate", LocalDate.now());
        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.of(streak));

        StreakResponseDto result = service.getStreak(USER_ID);

        assertEquals(5, result.currentStreak());
        assertEquals(10, result.longestStreak());
        assertTrue(result.streakActive());
        assertTrue(result.activityRegisteredToday());
    }

    // --- registerActivity ---

    @Test
    void registerActivity_firstTime_createsStreakOf1() {
        UserStreak streak = new UserStreak(USER_ID);
        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.of(streak));
        when(streakRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StreakResponseDto result = service.registerActivity(USER_ID);

        assertEquals(1, result.currentStreak());
        assertTrue(result.activityRegisteredToday());
    }

    @Test
    void registerActivity_consecutiveDay_incrementsStreak() throws Exception {
        UserStreak streak = new UserStreak(USER_ID);
        setField(streak, "currentStreak", 3);
        setField(streak, "longestStreak", 3);
        setField(streak, "lastActivityDate", LocalDate.now().minusDays(1));
        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.of(streak));
        when(streakRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StreakResponseDto result = service.registerActivity(USER_ID);

        assertEquals(4, result.currentStreak());
    }

    @Test
    void registerActivity_sameDayTwice_doesNotIncrement() throws Exception {
        UserStreak streak = new UserStreak(USER_ID);
        setField(streak, "currentStreak", 3);
        setField(streak, "longestStreak", 3);
        setField(streak, "lastActivityDate", LocalDate.now());
        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.of(streak));
        when(streakRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StreakResponseDto result = service.registerActivity(USER_ID);

        assertEquals(3, result.currentStreak());
    }

    @Test
    void registerActivity_afterBreak_resetsTo1() throws Exception {
        UserStreak streak = new UserStreak(USER_ID);
        setField(streak, "currentStreak", 7);
        setField(streak, "longestStreak", 7);
        setField(streak, "lastActivityDate", LocalDate.now().minusDays(3));
        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.of(streak));
        when(streakRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StreakResponseDto result = service.registerActivity(USER_ID);

        assertEquals(1, result.currentStreak());
    }

    @Test
    void registerActivity_beatsLongest_updatesLongestStreak() throws Exception {
        UserStreak streak = new UserStreak(USER_ID);
        setField(streak, "currentStreak", 10);
        setField(streak, "longestStreak", 10);
        setField(streak, "lastActivityDate", LocalDate.now().minusDays(1));
        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.of(streak));
        when(streakRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StreakResponseDto result = service.registerActivity(USER_ID);

        assertEquals(11, result.currentStreak());
        assertEquals(11, result.longestStreak());
    }

    @Test
    void registerActivity_noExistingStreak_createsAndSaves() {
        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(streakRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StreakResponseDto result = service.registerActivity(USER_ID);

        assertEquals(1, result.currentStreak());
        // Sparas en gång i getOrCreate + en gång efter registerActivity
        verify(streakRepository, times(2)).save(any(UserStreak.class));
    }

    // --- streakActive-logik i toDto ---

    @Test
    void toDto_streakActive_whenActivityToday() throws Exception {
        UserStreak streak = new UserStreak(USER_ID);
        setField(streak, "currentStreak", 5);
        setField(streak, "lastActivityDate", LocalDate.now());
        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.of(streak));

        StreakResponseDto result = service.getStreak(USER_ID);

        assertTrue(result.streakActive());
        assertTrue(result.activityRegisteredToday());
    }

    @Test
    void toDto_streakActive_whenActivityYesterday() throws Exception {
        UserStreak streak = new UserStreak(USER_ID);
        setField(streak, "currentStreak", 5);
        setField(streak, "lastActivityDate", LocalDate.now().minusDays(1));
        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.of(streak));

        StreakResponseDto result = service.getStreak(USER_ID);

        assertTrue(result.streakActive());
        assertFalse(result.activityRegisteredToday());
    }

    @Test
    void toDto_streakNotActive_whenActivityTwoDaysAgo() throws Exception {
        UserStreak streak = new UserStreak(USER_ID);
        setField(streak, "currentStreak", 5);
        setField(streak, "lastActivityDate", LocalDate.now().minusDays(2));
        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.of(streak));

        StreakResponseDto result = service.getStreak(USER_ID);

        assertFalse(result.streakActive());
        assertFalse(result.activityRegisteredToday());
    }

    @Test
    void toDto_noActivity_streakNotActive() {
        UserStreak streak = new UserStreak(USER_ID);
        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.of(streak));

        StreakResponseDto result = service.getStreak(USER_ID);

        assertFalse(result.streakActive());
        assertFalse(result.activityRegisteredToday());
    }

    // --- deleteAllForUser ---

    @Test
    void deleteAllForUser_delegatesToRepository() {
        service.deleteAllForUser(USER_ID);
        verify(streakRepository).deleteByUserId(USER_ID);
    }

    // --- Hjälpmetod: sätt privata/skyddade fält via reflektion ---

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field f = clazz.getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Fältet " + fieldName + " hittades inte i " + target.getClass());
    }
}
