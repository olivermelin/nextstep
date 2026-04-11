package se.sobriety.nextstep.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.sobriety.nextstep.dto.CollectibleDto;
import se.sobriety.nextstep.dto.DailyRewardDto;
import se.sobriety.nextstep.dto.StreakResponseDto;
import se.sobriety.nextstep.entity.Collectible;
import se.sobriety.nextstep.entity.DailyReward;
import se.sobriety.nextstep.entity.RewardType;
import se.sobriety.nextstep.repository.CollectibleRepository;
import se.sobriety.nextstep.repository.DailyRewardRepository;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RewardServiceTest {

    @Mock private DailyRewardRepository rewardRepository;
    @Mock private CollectibleRepository collectibleRepository;
    @Mock private UserProgressService progressService;
    @Mock private StreakService streakService;
    @Mock private Random mockRandom;

    private RewardService service;

    private static final String USER_ID = "user@test.com";

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        service = new RewardService(rewardRepository, collectibleRepository, progressService, streakService);

        // Ersätt internal Random med mockad version för deterministiska tester
        Field randomField = RewardService.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(service, mockRandom);

        // Standardbeteende: inte sällsynt, citatkort index 3
        when(mockRandom.nextDouble()).thenReturn(0.5);   // > 0.05 = inte sällsynt
        when(mockRandom.nextInt(anyInt())).thenReturn(3); // quoteIndex = 3
    }

    // --- getTodayReward ---

    @Test
    void getTodayReward_existing_returnsDto() {
        DailyReward reward = buildReward(RewardType.BONUS_XP, "{\"xp\":10}", 1, false);
        when(rewardRepository.findByUserIdAndRewardDate(USER_ID, LocalDate.now()))
                .thenReturn(Optional.of(reward));

        DailyRewardDto result = service.getTodayReward(USER_ID);

        assertNotNull(result);
        assertEquals("BONUS_XP", result.rewardType());
        assertFalse(result.claimed());
        assertEquals(1, result.consecutiveDays());
    }

    @Test
    void getTodayReward_noReward_returnsNull() {
        when(rewardRepository.findByUserIdAndRewardDate(USER_ID, LocalDate.now()))
                .thenReturn(Optional.empty());

        assertNull(service.getTodayReward(USER_ID));
    }

    // --- generateDailyReward ---

    @Test
    void generateDailyReward_existing_isIdempotent() {
        DailyReward existing = buildReward(RewardType.BONUS_XP, "{\"xp\":10}", 1, false);
        when(rewardRepository.findByUserIdAndRewardDate(USER_ID, LocalDate.now()))
                .thenReturn(Optional.of(existing));

        DailyRewardDto result = service.generateDailyReward(USER_ID);

        assertEquals("BONUS_XP", result.rewardType());
        verify(rewardRepository, never()).save(any());
        verify(streakService, never()).getStreak(any());
    }

    @Test
    void generateDailyReward_day1_typeIsBonusXpWith10Xp() {
        when(rewardRepository.findByUserIdAndRewardDate(USER_ID, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(streakService.getStreak(USER_ID)).thenReturn(streakDto(1));
        when(rewardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DailyRewardDto result = service.generateDailyReward(USER_ID);

        assertEquals("BONUS_XP", result.rewardType());
        assertEquals(10, extractXp(result.rewardValue()));
        assertFalse(result.rare());
    }

    @Test
    void generateDailyReward_day2_typeIsBonusXp() {
        when(rewardRepository.findByUserIdAndRewardDate(USER_ID, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(streakService.getStreak(USER_ID)).thenReturn(streakDto(2));
        when(rewardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DailyRewardDto result = service.generateDailyReward(USER_ID);

        assertEquals("BONUS_XP", result.rewardType());
        assertEquals(10, extractXp(result.rewardValue()));
    }

    @Test
    void generateDailyReward_day3_typeIsQuoteCardWith25Xp() {
        when(rewardRepository.findByUserIdAndRewardDate(USER_ID, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(streakService.getStreak(USER_ID)).thenReturn(streakDto(3));
        when(rewardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DailyRewardDto result = service.generateDailyReward(USER_ID);

        assertEquals("QUOTE_CARD", result.rewardType());
        assertEquals(25, extractXp(result.rewardValue()));
        assertTrue(result.rewardValue().contains("quoteId"));
    }

    @Test
    void generateDailyReward_day7_typeIsStreakFreezeWith50Xp() {
        when(rewardRepository.findByUserIdAndRewardDate(USER_ID, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(streakService.getStreak(USER_ID)).thenReturn(streakDto(7));
        when(rewardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DailyRewardDto result = service.generateDailyReward(USER_ID);

        assertEquals("STREAK_FREEZE", result.rewardType());
        assertEquals(50, extractXp(result.rewardValue()));
    }

    @Test
    void generateDailyReward_day14_xpIs100() {
        when(rewardRepository.findByUserIdAndRewardDate(USER_ID, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(streakService.getStreak(USER_ID)).thenReturn(streakDto(14));
        when(rewardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DailyRewardDto result = service.generateDailyReward(USER_ID);

        assertEquals(100, extractXp(result.rewardValue()));
    }

    @Test
    void generateDailyReward_rare_typeIsAvatarFrame() {
        when(mockRandom.nextDouble()).thenReturn(0.01); // < 0.05 = sällsynt
        when(rewardRepository.findByUserIdAndRewardDate(USER_ID, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(streakService.getStreak(USER_ID)).thenReturn(streakDto(5));
        when(rewardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DailyRewardDto result = service.generateDailyReward(USER_ID);

        assertEquals("AVATAR_FRAME", result.rewardType());
        assertTrue(result.rare());
        assertTrue(result.rewardValue().contains("frameId"));
    }

    @Test
    void generateDailyReward_rare_xpIsDoubled() {
        when(mockRandom.nextDouble()).thenReturn(0.01);
        when(rewardRepository.findByUserIdAndRewardDate(USER_ID, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(streakService.getStreak(USER_ID)).thenReturn(streakDto(3)); // normalt 25 XP → sällsynt = 50
        when(rewardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DailyRewardDto result = service.generateDailyReward(USER_ID);

        assertEquals(50, extractXp(result.rewardValue()));
    }

    // --- claimReward ---

    @Test
    void claimReward_unclaimed_marksClaimed() {
        DailyReward reward = buildReward(RewardType.BONUS_XP, "{\"xp\":10}", 1, false);
        when(rewardRepository.findByUserIdAndRewardDate(USER_ID, LocalDate.now()))
                .thenReturn(Optional.of(reward));
        when(rewardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DailyRewardDto result = service.claimReward(USER_ID);

        assertTrue(result.claimed());
    }

    @Test
    void claimReward_unclaimed_grantsXp() {
        DailyReward reward = buildReward(RewardType.BONUS_XP, "{\"xp\":25}", 3, false);
        when(rewardRepository.findByUserIdAndRewardDate(USER_ID, LocalDate.now()))
                .thenReturn(Optional.of(reward));
        when(rewardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.claimReward(USER_ID);

        verify(progressService).addPoints(USER_ID, 25);
    }

    @Test
    void claimReward_xpCappedAt100() {
        DailyReward reward = buildReward(RewardType.AVATAR_FRAME, "{\"xp\":200}", 14, true);
        when(rewardRepository.findByUserIdAndRewardDate(USER_ID, LocalDate.now()))
                .thenReturn(Optional.of(reward));
        when(rewardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collectibleRepository.findByUserIdAndCollectibleKey(any(), any()))
                .thenReturn(Optional.empty());

        service.claimReward(USER_ID);

        verify(progressService).addPoints(USER_ID, 100); // capped
    }

    @Test
    void claimReward_alreadyClaimed_noDoubleGrant() {
        DailyReward reward = buildReward(RewardType.BONUS_XP, "{\"xp\":25}", 3, false);
        reward.claim();
        when(rewardRepository.findByUserIdAndRewardDate(USER_ID, LocalDate.now()))
                .thenReturn(Optional.of(reward));

        service.claimReward(USER_ID);

        verify(progressService, never()).addPoints(any(), anyInt());
        verify(rewardRepository, never()).save(any());
    }

    @Test
    void claimReward_quoteCard_savesCollectible() {
        DailyReward reward = buildReward(RewardType.QUOTE_CARD,
                "{\"quoteId\":\"q3\",\"quote\":\"Test citat\",\"xp\":25}", 3, false);
        when(rewardRepository.findByUserIdAndRewardDate(USER_ID, LocalDate.now()))
                .thenReturn(Optional.of(reward));
        when(rewardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collectibleRepository.findByUserIdAndCollectibleKey(USER_ID, "q3"))
                .thenReturn(Optional.empty());

        service.claimReward(USER_ID);

        verify(collectibleRepository).save(any(Collectible.class));
    }

    @Test
    void claimReward_quoteCardAlreadyInCollection_doesNotDuplicate() {
        DailyReward reward = buildReward(RewardType.QUOTE_CARD,
                "{\"quoteId\":\"q3\",\"quote\":\"Test citat\",\"xp\":25}", 3, false);
        when(rewardRepository.findByUserIdAndRewardDate(USER_ID, LocalDate.now()))
                .thenReturn(Optional.of(reward));
        when(rewardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collectibleRepository.findByUserIdAndCollectibleKey(USER_ID, "q3"))
                .thenReturn(Optional.of(new Collectible(USER_ID, "q3", RewardType.QUOTE_CARD)));

        service.claimReward(USER_ID);

        verify(collectibleRepository, never()).save(any());
    }

    @Test
    void claimReward_bonusXp_doesNotSaveCollectible() {
        DailyReward reward = buildReward(RewardType.BONUS_XP, "{\"xp\":10}", 1, false);
        when(rewardRepository.findByUserIdAndRewardDate(USER_ID, LocalDate.now()))
                .thenReturn(Optional.of(reward));
        when(rewardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.claimReward(USER_ID);

        verify(collectibleRepository, never()).save(any());
    }

    @Test
    void claimReward_noReward_throwsIllegalStateException() {
        when(rewardRepository.findByUserIdAndRewardDate(USER_ID, LocalDate.now()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> service.claimReward(USER_ID));
    }

    // --- getCollection ---

    @Test
    void getCollection_returnsAllCollectibles() {
        Collectible c1 = buildCollectible("q1", RewardType.QUOTE_CARD);
        Collectible c2 = buildCollectible("frame_gold_1", RewardType.AVATAR_FRAME);
        when(collectibleRepository.findByUserId(USER_ID)).thenReturn(List.of(c1, c2));

        List<CollectibleDto> result = service.getCollection(USER_ID);

        assertEquals(2, result.size());
        assertEquals("q1", result.get(0).collectibleKey());
        assertEquals("QUOTE_CARD", result.get(0).collectibleType());
        assertEquals("frame_gold_1", result.get(1).collectibleKey());
        assertEquals("AVATAR_FRAME", result.get(1).collectibleType());
    }

    @Test
    void getCollection_empty_returnsEmptyList() {
        when(collectibleRepository.findByUserId(USER_ID)).thenReturn(List.of());

        List<CollectibleDto> result = service.getCollection(USER_ID);

        assertTrue(result.isEmpty());
    }

    // --- Hjälpmetoder ---

    private StreakResponseDto streakDto(int currentStreak) {
        return new StreakResponseDto(
                USER_ID, currentStreak, currentStreak,
                LocalDate.now().minusDays(1),
                LocalDate.now().minusDays(Math.max(0, currentStreak - 1L)),
                true, false
        );
    }

    private DailyReward buildReward(RewardType type, String value, int consecutive, boolean rare) {
        return new DailyReward(USER_ID, LocalDate.now(), type, value, consecutive, rare);
    }

    /** Skapar ett Collectible med unlockedAt satt — simulerar @PrePersist som JPA normalt kör. */
    private Collectible buildCollectible(String key, RewardType type) {
        Collectible c = new Collectible(USER_ID, key, type);
        try {
            java.lang.reflect.Field f = Collectible.class.getDeclaredField("unlockedAt");
            f.setAccessible(true);
            f.set(c, java.time.LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return c;
    }

    private int extractXp(String json) {
        int idx = json.indexOf("\"xp\":");
        if (idx < 0) return -1;
        String after = json.substring(idx + 5).replaceAll("[^0-9]", " ").trim();
        return Integer.parseInt(after.split(" ")[0]);
    }
}
