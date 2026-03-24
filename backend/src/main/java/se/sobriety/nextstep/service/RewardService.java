package se.sobriety.nextstep.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.dto.CollectibleDto;
import se.sobriety.nextstep.dto.DailyRewardDto;
import se.sobriety.nextstep.entity.Collectible;
import se.sobriety.nextstep.entity.DailyReward;
import se.sobriety.nextstep.entity.RewardType;
import se.sobriety.nextstep.repository.CollectibleRepository;
import se.sobriety.nextstep.repository.DailyRewardRepository;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@Transactional(readOnly = true)
public class RewardService {

    private static final int TOTAL_QUOTES = 20;
    private static final int TOTAL_FRAMES = 5;
    private static final double RARE_CHANCE = 0.05;

    private static final List<String> QUOTES = List.of(
            "Varje dag du väljer dig själv är en seger.",
            "Styrka växer inte ur bekvämlighet, utan ur motstånd.",
            "Du behöver inte vara perfekt, bara modig nog att fortsätta.",
            "En dag i taget — det är allt som behövs.",
            "Din resa inspirerar fler än du tror.",
            "Mod är inte frånvaro av rädsla, utan att agera trots den.",
            "Du har redan överlevt dina svåraste dagar.",
            "Framsteg, inte perfektion.",
            "Varje steg framåt räknas, oavsett hur litet.",
            "Du förtjänar det liv du kämpar för.",
            "Sårbarhet är inte svaghet — det är mod.",
            "Idag är en ny chans att växa.",
            "Du är starkare än du tror.",
            "Förändring börjar med ett enda val.",
            "Ditt värde definieras inte av ditt förflutna.",
            "Att be om hjälp är ett tecken på styrka.",
            "Små framsteg varje dag leder till stora resultat.",
            "Du är inte ensam på den här resan.",
            "Tro på den du håller på att bli.",
            "Varje morgon är en ny början."
    );

    private final DailyRewardRepository rewardRepository;
    private final CollectibleRepository collectibleRepository;
    private final UserProgressService progressService;
    private final StreakService streakService;
    private final Random random = new Random();

    public RewardService(DailyRewardRepository rewardRepository,
                         CollectibleRepository collectibleRepository,
                         UserProgressService progressService,
                         StreakService streakService) {
        this.rewardRepository = rewardRepository;
        this.collectibleRepository = collectibleRepository;
        this.progressService = progressService;
        this.streakService = streakService;
    }

    /**
     * Generera dagens belöning om den inte redan finns.
     * Returnerar null om belöning redan skapats.
     */
    @Transactional
    public DailyRewardDto generateDailyReward(String userId) {
        LocalDate today = LocalDate.now();
        Optional<DailyReward> existing = rewardRepository.findByUserIdAndRewardDate(userId, today);

        if (existing.isPresent()) {
            return toDto(existing.get());
        }

        var streakData = streakService.getStreakData(userId, 1);
        int consecutiveDays = streakData.currentStreak();
        boolean isRare = random.nextDouble() < RARE_CHANCE;

        RewardType type;
        String value;

        if (isRare) {
            // Sällsynt belöning: special avatar-ram
            int frameIndex = (consecutiveDays % TOTAL_FRAMES) + 1;
            type = RewardType.AVATAR_FRAME;
            value = "{\"frameId\":\"frame_gold_" + frameIndex + "\",\"xp\":" + calculateXp(consecutiveDays) * 2 + "}";
        } else if (consecutiveDays >= 7 && consecutiveDays % 7 == 0) {
            // Var 7:e dag: streak freeze
            type = RewardType.STREAK_FREEZE;
            value = "{\"xp\":" + calculateXp(consecutiveDays) + "}";
        } else if (consecutiveDays >= 3) {
            // Dag 3+: citat-kort + XP
            int quoteIndex = random.nextInt(TOTAL_QUOTES);
            type = RewardType.QUOTE_CARD;
            value = "{\"quoteId\":\"q" + quoteIndex + "\",\"quote\":\"" +
                    escapeJson(QUOTES.get(quoteIndex)) + "\",\"xp\":" + calculateXp(consecutiveDays) + "}";
        } else {
            // Dag 1-2: bara bonus XP
            type = RewardType.BONUS_XP;
            value = "{\"xp\":" + calculateXp(consecutiveDays) + "}";
        }

        DailyReward reward = new DailyReward(userId, today, type, value, consecutiveDays, isRare);
        rewardRepository.save(reward);

        log.info("Generated daily reward for user {}: type={}, consecutive={}, rare={}",
                userId, type, consecutiveDays, isRare);

        return toDto(reward);
    }

    /**
     * Claima dagens belöning: ge XP och spara eventuellt collectible.
     */
    @Transactional
    public DailyRewardDto claimReward(String userId) {
        LocalDate today = LocalDate.now();
        DailyReward reward = rewardRepository.findByUserIdAndRewardDate(userId, today)
                .orElseThrow(() -> new IllegalStateException("No reward generated for today"));

        if (reward.isClaimed()) {
            return toDto(reward);
        }

        reward.claim();
        rewardRepository.save(reward);

        // Extrahera XP från value JSON
        int xp = extractXp(reward.getRewardValue());
        if (xp > 0) {
            progressService.addPoints(userId, Math.min(xp, 100));
        }

        // Spara collectible om det är ett citat-kort eller avatar-ram
        if (reward.getRewardType() == RewardType.QUOTE_CARD ||
            reward.getRewardType() == RewardType.AVATAR_FRAME) {
            String key = extractCollectibleKey(reward);
            if (collectibleRepository.findByUserIdAndCollectibleKey(userId, key).isEmpty()) {
                collectibleRepository.save(new Collectible(userId, key, reward.getRewardType()));
            }
        }

        return toDto(reward);
    }

    /**
     * Hämta dagens belöning (utan att generera ny).
     */
    public DailyRewardDto getTodayReward(String userId) {
        return rewardRepository.findByUserIdAndRewardDate(userId, LocalDate.now())
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * Hämta användarens samling.
     */
    public List<CollectibleDto> getCollection(String userId) {
        return collectibleRepository.findByUserId(userId).stream()
                .map(c -> new CollectibleDto(
                        c.getCollectibleKey(),
                        c.getCollectibleType().name(),
                        c.getUnlockedAt().toString()
                ))
                .toList();
    }

    private int calculateXp(int consecutiveDays) {
        if (consecutiveDays >= 14) return 100;
        if (consecutiveDays >= 7) return 50;
        if (consecutiveDays >= 3) return 25;
        return 10;
    }

    private int extractXp(String valueJson) {
        if (valueJson == null) return 0;
        try {
            int idx = valueJson.indexOf("\"xp\":");
            if (idx < 0) return 0;
            String after = valueJson.substring(idx + 5).replaceAll("[^0-9]", " ").trim();
            return Integer.parseInt(after.split(" ")[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    private String extractCollectibleKey(DailyReward reward) {
        if (reward.getRewardType() == RewardType.QUOTE_CARD) {
            String value = reward.getRewardValue();
            int idx = value.indexOf("\"quoteId\":\"");
            if (idx >= 0) {
                int start = idx + 11;
                int end = value.indexOf("\"", start);
                return value.substring(start, end);
            }
            return "q" + reward.getRewardDate().toString();
        }
        if (reward.getRewardType() == RewardType.AVATAR_FRAME) {
            String value = reward.getRewardValue();
            int idx = value.indexOf("\"frameId\":\"");
            if (idx >= 0) {
                int start = idx + 11;
                int end = value.indexOf("\"", start);
                return value.substring(start, end);
            }
            return "frame_" + reward.getRewardDate().toString();
        }
        return "reward_" + reward.getRewardDate().toString();
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private DailyRewardDto toDto(DailyReward r) {
        return new DailyRewardDto(
                r.getId(),
                r.getRewardDate().toString(),
                r.getRewardType().name(),
                r.getRewardValue(),
                r.isClaimed(),
                r.getConsecutiveDays(),
                r.isRare()
        );
    }
}
