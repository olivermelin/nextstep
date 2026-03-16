package se.sobriety.nextstep.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.sobriety.nextstep.dto.UserChallengeOutDto;
import se.sobriety.nextstep.entity.Challenge;
import se.sobriety.nextstep.entity.ChallengeCategory;
import se.sobriety.nextstep.entity.ChallengeDifficulty;
import se.sobriety.nextstep.entity.UserChallenge;
import se.sobriety.nextstep.mapper.UserChallengeMapper;
import se.sobriety.nextstep.repository.ChallengeRepository;
import se.sobriety.nextstep.repository.UserChallengeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserChallengeServiceTest {

    @Mock
    private UserChallengeRepository userChallengeRepository;
    @Mock
    private ChallengeRepository challengeRepository;
    @Mock
    private UserChallengeMapper userChallengeMapper;
    @Mock
    private UserProgressService userProgressService;

    private UserChallengeService service;

    private static final String USER_ID = "user@test.com";
    private static final Long CHALLENGE_ID = 1L;

    private Challenge testChallenge;
    private UserChallengeOutDto testDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UserChallengeService(userChallengeRepository, challengeRepository, userChallengeMapper, userProgressService);

        testChallenge = new Challenge("Test Challenge", "Description", 15,
                ChallengeDifficulty.EASY, ChallengeCategory.MENTAL_HEALTH);
        testChallenge.setId(CHALLENGE_ID);

        testDto = new UserChallengeOutDto(1L, CHALLENGE_ID, "Test Challenge",
                "MENTAL_HEALTH", "EASY", 15, "IN_PROGRESS",
                LocalDateTime.now(), null, 0);
    }

    // --- startChallenge ---

    @Test
    void startChallenge_newChallenge_createsAndReturns() {
        when(userChallengeRepository.existsCompletedSince(eq(USER_ID), eq(CHALLENGE_ID), any())).thenReturn(false);
        when(userChallengeRepository.findTopByUserIdAndChallengeIdAndCompletedOrderByStartedAtDesc(USER_ID, CHALLENGE_ID, false))
                .thenReturn(Optional.empty());
        when(userChallengeRepository.findByUserIdAndCompleted(USER_ID, false)).thenReturn(List.of());
        when(challengeRepository.findById(CHALLENGE_ID)).thenReturn(Optional.of(testChallenge));
        when(userChallengeRepository.save(any(UserChallenge.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userChallengeMapper.toDto(any())).thenReturn(testDto);

        UserChallengeOutDto result = service.startChallenge(USER_ID, CHALLENGE_ID);

        assertNotNull(result);
        verify(userChallengeRepository).save(any(UserChallenge.class));
    }

    @Test
    void startChallenge_alreadyCompletedToday_throwsException() {
        when(userChallengeRepository.existsCompletedSince(eq(USER_ID), eq(CHALLENGE_ID), any())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.startChallenge(USER_ID, CHALLENGE_ID));
        assertTrue(ex.getMessage().contains("already completed today"));
    }

    @Test
    void startChallenge_existingActiveChallenge_returnsExisting() {
        UserChallenge existing = new UserChallenge(USER_ID, testChallenge);
        when(userChallengeRepository.existsCompletedSince(eq(USER_ID), eq(CHALLENGE_ID), any())).thenReturn(false);
        when(userChallengeRepository.findTopByUserIdAndChallengeIdAndCompletedOrderByStartedAtDesc(USER_ID, CHALLENGE_ID, false))
                .thenReturn(Optional.of(existing));
        when(userChallengeMapper.toDto(existing)).thenReturn(testDto);

        UserChallengeOutDto result = service.startChallenge(USER_ID, CHALLENGE_ID);

        assertNotNull(result);
        verify(userChallengeRepository, never()).save(any());
    }

    @Test
    void startChallenge_anotherChallengeActive_throwsException() {
        Challenge otherChallenge = new Challenge("Other", "Desc", 10,
                ChallengeDifficulty.MEDIUM, ChallengeCategory.PHYSICAL_ACTIVITY);
        otherChallenge.setId(99L);
        UserChallenge activeOther = new UserChallenge(USER_ID, otherChallenge);
        activeOther.setStartedAt(LocalDateTime.now()); // today

        when(userChallengeRepository.existsCompletedSince(eq(USER_ID), eq(CHALLENGE_ID), any())).thenReturn(false);
        when(userChallengeRepository.findTopByUserIdAndChallengeIdAndCompletedOrderByStartedAtDesc(USER_ID, CHALLENGE_ID, false))
                .thenReturn(Optional.empty());
        when(userChallengeRepository.findByUserIdAndCompleted(USER_ID, false)).thenReturn(List.of(activeOther));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.startChallenge(USER_ID, CHALLENGE_ID));
        assertTrue(ex.getMessage().contains("pågående aktivitet"));
    }

    @Test
    void startChallenge_challengeNotFound_throwsException() {
        when(userChallengeRepository.existsCompletedSince(eq(USER_ID), eq(CHALLENGE_ID), any())).thenReturn(false);
        when(userChallengeRepository.findTopByUserIdAndChallengeIdAndCompletedOrderByStartedAtDesc(USER_ID, CHALLENGE_ID, false))
                .thenReturn(Optional.empty());
        when(userChallengeRepository.findByUserIdAndCompleted(USER_ID, false)).thenReturn(List.of());
        when(challengeRepository.findById(CHALLENGE_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.startChallenge(USER_ID, CHALLENGE_ID));
    }

    // --- completeChallenge ---

    @Test
    void completeChallenge_validActive_completesAndAwardsPoints() {
        UserChallenge active = new UserChallenge(USER_ID, testChallenge);
        when(userChallengeRepository.existsCompletedSince(eq(USER_ID), eq(CHALLENGE_ID), any())).thenReturn(false);
        when(userChallengeRepository.findTopByUserIdAndChallengeIdAndCompletedOrderByStartedAtDesc(USER_ID, CHALLENGE_ID, false))
                .thenReturn(Optional.of(active));
        when(userChallengeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        UserChallengeOutDto completedDto = new UserChallengeOutDto(1L, CHALLENGE_ID, "Test",
                "MENTAL_HEALTH", "EASY", 15, "COMPLETED",
                LocalDateTime.now(), LocalDateTime.now(), 10);
        when(userChallengeMapper.toDto(any())).thenReturn(completedDto);

        UserChallengeOutDto result = service.completeChallenge(USER_ID, CHALLENGE_ID);

        assertEquals("COMPLETED", result.status());
        verify(userProgressService).addPoints(eq(USER_ID), anyInt());
    }

    @Test
    void completeChallenge_alreadyCompletedToday_throwsException() {
        when(userChallengeRepository.existsCompletedSince(eq(USER_ID), eq(CHALLENGE_ID), any())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.completeChallenge(USER_ID, CHALLENGE_ID));
    }

    @Test
    void completeChallenge_noActiveChallenge_throwsException() {
        when(userChallengeRepository.existsCompletedSince(eq(USER_ID), eq(CHALLENGE_ID), any())).thenReturn(false);
        when(userChallengeRepository.findTopByUserIdAndChallengeIdAndCompletedOrderByStartedAtDesc(USER_ID, CHALLENGE_ID, false))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.completeChallenge(USER_ID, CHALLENGE_ID));
    }

    // --- getUserChallenges ---

    @Test
    void getUserCompletedChallenges_returnsOnlyCompleted() {
        UserChallenge completed = new UserChallenge(USER_ID, testChallenge);
        completed.complete();
        when(userChallengeRepository.findByUserIdAndCompleted(USER_ID, true)).thenReturn(List.of(completed));
        when(userChallengeMapper.toDto(any())).thenReturn(testDto);

        List<UserChallengeOutDto> result = service.getUserCompletedChallenges(USER_ID);

        assertEquals(1, result.size());
    }
}
