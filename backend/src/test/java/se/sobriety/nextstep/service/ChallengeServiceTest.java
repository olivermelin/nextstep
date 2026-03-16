package se.sobriety.nextstep.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.sobriety.nextstep.dto.ChallengeInDto;
import se.sobriety.nextstep.dto.ChallengeOutDto;
import se.sobriety.nextstep.entity.Challenge;
import se.sobriety.nextstep.entity.ChallengeCategory;
import se.sobriety.nextstep.entity.ChallengeDifficulty;
import se.sobriety.nextstep.mapper.ChallengeMapper;
import se.sobriety.nextstep.repository.ChallengeRepository;
import se.sobriety.nextstep.repository.UserChallengeRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChallengeServiceTest {

    @Mock
    private ChallengeRepository challengeRepository;
    @Mock
    private ChallengeMapper challengeMapper;
    @Mock
    private UserChallengeRepository userChallengeRepository;

    private ChallengeService service;

    private static final String USER_ID = "user@test.com";
    private static final Long CHALLENGE_ID = 1L;

    private Challenge testChallenge;
    private ChallengeOutDto testDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ChallengeService(challengeRepository, challengeMapper, userChallengeRepository);

        testChallenge = new Challenge("Test Challenge", "A test description", 15,
                ChallengeDifficulty.EASY, ChallengeCategory.MENTAL_HEALTH);
        testChallenge.setId(CHALLENGE_ID);

        testDto = new ChallengeOutDto(CHALLENGE_ID, "Test Challenge", "A test description",
                15, "EASY", "MENTAL_HEALTH", null, null,
                LocalDateTime.now(), false);
    }

    // --- getAllChallenges ---

    @Test
    void getAllChallenges_returnsMappedDtos() {
        Challenge second = new Challenge("Second", "Desc", 30,
                ChallengeDifficulty.MEDIUM, ChallengeCategory.PHYSICAL_ACTIVITY);
        second.setId(2L);
        ChallengeOutDto secondDto = new ChallengeOutDto(2L, "Second", "Desc",
                30, "MEDIUM", "PHYSICAL_ACTIVITY", null, null,
                LocalDateTime.now(), false);

        when(challengeRepository.findAll()).thenReturn(List.of(testChallenge, second));
        when(challengeMapper.toDto(testChallenge)).thenReturn(testDto);
        when(challengeMapper.toDto(second)).thenReturn(secondDto);

        List<ChallengeOutDto> result = service.getAllChallenges();

        assertEquals(2, result.size());
        assertEquals(testDto, result.get(0));
        assertEquals(secondDto, result.get(1));
        verify(challengeRepository).findAll();
        verify(challengeMapper, times(2)).toDto(any(Challenge.class));
    }

    @Test
    void getAllChallenges_emptyList_returnsEmpty() {
        when(challengeRepository.findAll()).thenReturn(Collections.emptyList());

        List<ChallengeOutDto> result = service.getAllChallenges();

        assertTrue(result.isEmpty());
    }

    // --- getChallengeById ---

    @Test
    void getChallengeById_found_returnsDto() {
        when(challengeRepository.findById(CHALLENGE_ID)).thenReturn(Optional.of(testChallenge));
        when(challengeMapper.toDto(testChallenge)).thenReturn(testDto);

        ChallengeOutDto result = service.getChallengeById(CHALLENGE_ID);

        assertNotNull(result);
        assertEquals(CHALLENGE_ID, result.id());
        assertEquals("Test Challenge", result.title());
        verify(challengeRepository).findById(CHALLENGE_ID);
    }

    @Test
    void getChallengeById_notFound_throwsException() {
        when(challengeRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getChallengeById(99L));
        assertTrue(ex.getMessage().contains("Challenge not found"));
        assertTrue(ex.getMessage().contains("99"));
    }

    // --- getChallengesByCategory ---

    @Test
    void getChallengesByCategory_returnsFiltered() {
        when(challengeRepository.findByCategory(ChallengeCategory.MENTAL_HEALTH))
                .thenReturn(List.of(testChallenge));
        when(challengeMapper.toDto(testChallenge)).thenReturn(testDto);

        List<ChallengeOutDto> result = service.getChallengesByCategory(ChallengeCategory.MENTAL_HEALTH);

        assertEquals(1, result.size());
        assertEquals("MENTAL_HEALTH", result.get(0).category());
        verify(challengeRepository).findByCategory(ChallengeCategory.MENTAL_HEALTH);
    }

    @Test
    void getChallengesByCategory_noMatches_returnsEmpty() {
        when(challengeRepository.findByCategory(ChallengeCategory.SOCIAL_SKILLS))
                .thenReturn(Collections.emptyList());

        List<ChallengeOutDto> result = service.getChallengesByCategory(ChallengeCategory.SOCIAL_SKILLS);

        assertTrue(result.isEmpty());
    }

    // --- getAllChallengesForUser ---

    @Test
    void getAllChallengesForUser_withCompletedToday_setsFlag() {
        ChallengeOutDto completedDto = new ChallengeOutDto(CHALLENGE_ID, "Test Challenge",
                "A test description", 15, "EASY", "MENTAL_HEALTH",
                null, null, LocalDateTime.now(), true);

        when(challengeRepository.findAll()).thenReturn(List.of(testChallenge));
        when(userChallengeRepository.findCompletedChallengeIdsSince(eq(USER_ID), any()))
                .thenReturn(List.of(CHALLENGE_ID));
        when(challengeMapper.toDto(testChallenge, true)).thenReturn(completedDto);

        List<ChallengeOutDto> result = service.getAllChallengesForUser(USER_ID);

        assertEquals(1, result.size());
        assertTrue(result.get(0).completedToday());
        verify(challengeMapper).toDto(testChallenge, true);
    }

    @Test
    void getAllChallengesForUser_notCompletedToday_flagIsFalse() {
        when(challengeRepository.findAll()).thenReturn(List.of(testChallenge));
        when(userChallengeRepository.findCompletedChallengeIdsSince(eq(USER_ID), any()))
                .thenReturn(Collections.emptyList());
        when(challengeMapper.toDto(testChallenge, false)).thenReturn(testDto);

        List<ChallengeOutDto> result = service.getAllChallengesForUser(USER_ID);

        assertEquals(1, result.size());
        assertFalse(result.get(0).completedToday());
        verify(challengeMapper).toDto(testChallenge, false);
    }

    @Test
    void getAllChallengesForUser_multipleChallenges_mixedCompletion() {
        Challenge second = new Challenge("Second", "Desc", 30,
                ChallengeDifficulty.MEDIUM, ChallengeCategory.PHYSICAL_ACTIVITY);
        second.setId(2L);

        ChallengeOutDto completedDto = new ChallengeOutDto(CHALLENGE_ID, "Test Challenge",
                "A test description", 15, "EASY", "MENTAL_HEALTH",
                null, null, LocalDateTime.now(), true);
        ChallengeOutDto notCompletedDto = new ChallengeOutDto(2L, "Second", "Desc",
                30, "MEDIUM", "PHYSICAL_ACTIVITY", null, null,
                LocalDateTime.now(), false);

        when(challengeRepository.findAll()).thenReturn(List.of(testChallenge, second));
        when(userChallengeRepository.findCompletedChallengeIdsSince(eq(USER_ID), any()))
                .thenReturn(List.of(CHALLENGE_ID));
        when(challengeMapper.toDto(testChallenge, true)).thenReturn(completedDto);
        when(challengeMapper.toDto(second, false)).thenReturn(notCompletedDto);

        List<ChallengeOutDto> result = service.getAllChallengesForUser(USER_ID);

        assertEquals(2, result.size());
        assertTrue(result.get(0).completedToday());
        assertFalse(result.get(1).completedToday());
    }

    // --- getChallengesByCategoryForUser ---

    @Test
    void getChallengesByCategoryForUser_withCompletedToday() {
        ChallengeOutDto completedDto = new ChallengeOutDto(CHALLENGE_ID, "Test Challenge",
                "A test description", 15, "EASY", "MENTAL_HEALTH",
                null, null, LocalDateTime.now(), true);

        when(challengeRepository.findByCategory(ChallengeCategory.MENTAL_HEALTH))
                .thenReturn(List.of(testChallenge));
        when(userChallengeRepository.findCompletedChallengeIdsSince(eq(USER_ID), any()))
                .thenReturn(List.of(CHALLENGE_ID));
        when(challengeMapper.toDto(testChallenge, true)).thenReturn(completedDto);

        List<ChallengeOutDto> result = service.getChallengesByCategoryForUser(
                ChallengeCategory.MENTAL_HEALTH, USER_ID);

        assertEquals(1, result.size());
        assertTrue(result.get(0).completedToday());
        verify(challengeRepository).findByCategory(ChallengeCategory.MENTAL_HEALTH);
    }

    @Test
    void getChallengesByCategoryForUser_notCompletedToday() {
        when(challengeRepository.findByCategory(ChallengeCategory.MENTAL_HEALTH))
                .thenReturn(List.of(testChallenge));
        when(userChallengeRepository.findCompletedChallengeIdsSince(eq(USER_ID), any()))
                .thenReturn(Collections.emptyList());
        when(challengeMapper.toDto(testChallenge, false)).thenReturn(testDto);

        List<ChallengeOutDto> result = service.getChallengesByCategoryForUser(
                ChallengeCategory.MENTAL_HEALTH, USER_ID);

        assertEquals(1, result.size());
        assertFalse(result.get(0).completedToday());
    }

    // --- createChallenge ---

    @Test
    void createChallenge_validInput_savesAndReturnsDto() {
        ChallengeInDto inDto = new ChallengeInDto("New Challenge", "New description",
                20, "MEDIUM", "PHYSICAL_ACTIVITY",
                "https://youtube.com/watch?v=123", "Step 1: Do this");

        ChallengeOutDto createdDto = new ChallengeOutDto(2L, "New Challenge", "New description",
                20, "MEDIUM", "PHYSICAL_ACTIVITY",
                "https://youtube.com/watch?v=123", "Step 1: Do this",
                LocalDateTime.now(), false);

        when(challengeRepository.save(any(Challenge.class))).thenAnswer(inv -> {
            Challenge saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        when(challengeMapper.toDto(any(Challenge.class))).thenReturn(createdDto);

        ChallengeOutDto result = service.createChallenge(inDto);

        assertNotNull(result);
        assertEquals("New Challenge", result.title());
        assertEquals("MEDIUM", result.difficulty());
        assertEquals("PHYSICAL_ACTIVITY", result.category());
        verify(challengeRepository).save(any(Challenge.class));
    }

    @Test
    void createChallenge_setsYoutubeUrlAndInstructions() {
        ChallengeInDto inDto = new ChallengeInDto("Title", "Desc", 10, "EASY",
                "MENTAL_HEALTH", "https://youtube.com/test", "Instructions here");

        when(challengeRepository.save(any(Challenge.class))).thenAnswer(inv -> {
            Challenge saved = inv.getArgument(0);
            saved.setId(3L);
            return saved;
        });
        when(challengeMapper.toDto(any(Challenge.class))).thenReturn(testDto);

        service.createChallenge(inDto);

        verify(challengeRepository).save(argThat(challenge ->
                "https://youtube.com/test".equals(challenge.getYoutubeUrl()) &&
                "Instructions here".equals(challenge.getInstructions())
        ));
    }

    @Test
    void createChallenge_nullOptionalFields_savesSuccessfully() {
        ChallengeInDto inDto = new ChallengeInDto("Title", "Desc", 10, "HARD",
                "FOCUS_DISCIPLINE", null, null);

        when(challengeRepository.save(any(Challenge.class))).thenAnswer(inv -> {
            Challenge saved = inv.getArgument(0);
            saved.setId(4L);
            return saved;
        });
        when(challengeMapper.toDto(any(Challenge.class))).thenReturn(testDto);

        ChallengeOutDto result = service.createChallenge(inDto);

        assertNotNull(result);
        verify(challengeRepository).save(argThat(challenge ->
                challenge.getYoutubeUrl() == null && challenge.getInstructions() == null
        ));
    }

    // --- updateChallenge ---

    @Test
    void updateChallenge_valid_updatesAndReturnsDto() {
        ChallengeInDto inDto = new ChallengeInDto("Updated Title", "Updated desc",
                45, "HARD", "PHYSICAL_ACTIVITY", "https://youtube.com/new", "New instructions");

        ChallengeOutDto updatedDto = new ChallengeOutDto(CHALLENGE_ID, "Updated Title", "Updated desc",
                45, "HARD", "PHYSICAL_ACTIVITY", "https://youtube.com/new", "New instructions",
                LocalDateTime.now(), false);

        when(challengeRepository.findById(CHALLENGE_ID)).thenReturn(Optional.of(testChallenge));
        when(challengeRepository.save(any(Challenge.class))).thenAnswer(inv -> inv.getArgument(0));
        when(challengeMapper.toDto(any(Challenge.class))).thenReturn(updatedDto);

        ChallengeOutDto result = service.updateChallenge(CHALLENGE_ID, inDto);

        assertNotNull(result);
        assertEquals("Updated Title", result.title());
        assertEquals("HARD", result.difficulty());
        verify(challengeRepository).findById(CHALLENGE_ID);
        verify(challengeRepository).save(any(Challenge.class));
    }

    @Test
    void updateChallenge_setsAllFields() {
        ChallengeInDto inDto = new ChallengeInDto("New Title", "New desc",
                60, "MEDIUM", "SOCIAL_SKILLS", "https://yt.com", "Do things");

        when(challengeRepository.findById(CHALLENGE_ID)).thenReturn(Optional.of(testChallenge));
        when(challengeRepository.save(any(Challenge.class))).thenAnswer(inv -> inv.getArgument(0));
        when(challengeMapper.toDto(any(Challenge.class))).thenReturn(testDto);

        service.updateChallenge(CHALLENGE_ID, inDto);

        verify(challengeRepository).save(argThat(challenge ->
                "New Title".equals(challenge.getTitle()) &&
                "New desc".equals(challenge.getDescription()) &&
                challenge.getDurationMinutes() == 60 &&
                challenge.getDifficulty() == ChallengeDifficulty.MEDIUM &&
                challenge.getCategory() == ChallengeCategory.SOCIAL_SKILLS &&
                "https://yt.com".equals(challenge.getYoutubeUrl()) &&
                "Do things".equals(challenge.getInstructions())
        ));
    }

    @Test
    void updateChallenge_notFound_throwsException() {
        ChallengeInDto inDto = new ChallengeInDto("Title", "Desc", 10, "EASY",
                "MENTAL_HEALTH", null, null);

        when(challengeRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateChallenge(99L, inDto));
        assertTrue(ex.getMessage().contains("Challenge not found"));
        assertTrue(ex.getMessage().contains("99"));
        verify(challengeRepository, never()).save(any());
    }

    // --- deleteChallenge ---

    @Test
    void deleteChallenge_exists_deletesSuccessfully() {
        when(challengeRepository.existsById(CHALLENGE_ID)).thenReturn(true);

        assertDoesNotThrow(() -> service.deleteChallenge(CHALLENGE_ID));

        verify(challengeRepository).existsById(CHALLENGE_ID);
        verify(challengeRepository).deleteById(CHALLENGE_ID);
    }

    @Test
    void deleteChallenge_notFound_throwsException() {
        when(challengeRepository.existsById(99L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.deleteChallenge(99L));
        assertTrue(ex.getMessage().contains("Challenge not found"));
        assertTrue(ex.getMessage().contains("99"));
        verify(challengeRepository, never()).deleteById(any());
    }
}
