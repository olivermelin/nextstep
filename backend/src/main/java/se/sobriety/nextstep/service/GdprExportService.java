package se.sobriety.nextstep.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.entity.UserSettings;
import se.sobriety.nextstep.repository.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GDPR Artikel 20 — Rätt till dataportabilitet.
 * Samlar all persondata för en användare och returnerar den som en strukturerad Map
 * som kontrollern serialiserar till JSON.
 */
@Service
@Transactional(readOnly = true)
public class GdprExportService {

    private final UserSettingsService userSettingsService;
    private final DailyCheckInRepository checkInRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final UserStreakRepository streakRepository;
    private final DailyRewardRepository rewardRepository;
    private final CollectibleRepository collectibleRepository;
    private final ActivityFeedService activityFeedService;

    public GdprExportService(
            UserSettingsService userSettingsService,
            DailyCheckInRepository checkInRepository,
            UserChallengeRepository userChallengeRepository,
            UserStreakRepository streakRepository,
            DailyRewardRepository rewardRepository,
            CollectibleRepository collectibleRepository,
            ActivityFeedService activityFeedService) {
        this.userSettingsService = userSettingsService;
        this.checkInRepository = checkInRepository;
        this.userChallengeRepository = userChallengeRepository;
        this.streakRepository = streakRepository;
        this.rewardRepository = rewardRepository;
        this.collectibleRepository = collectibleRepository;
        this.activityFeedService = activityFeedService;
    }

    /**
     * Exporterar all data kopplad till användaren.
     */
    public Map<String, Object> exportUserData(String userId) {
        Map<String, Object> export = new LinkedHashMap<>();

        export.put("exportedAt", LocalDateTime.now().toString());
        export.put("userId", userId);

        // Profilinställningar
        try {
            UserSettings settings = userSettingsService.getOrCreateSettings(userId);
            Map<String, Object> profile = new LinkedHashMap<>();
            profile.put("name", settings.getName());
            profile.put("email", settings.getEmail());
            profile.put("phone", settings.getPhone());
            profile.put("language", settings.getLanguage());
            profile.put("recoveryStage", settings.getRecoveryStage());
            profile.put("onboardingTrack", settings.getOnboardingTrack());
            profile.put("coachPersonality", settings.getCoachPersonality());
            export.put("profile", profile);
        } catch (Exception ignored) {}

        // Incheckningshistorik
        try {
            var checkIns = checkInRepository.findByUserIdOrderByCheckInDateDesc(userId)
                    .stream()
                    .map(c -> Map.of(
                            "date", c.getCheckInDate().toString(),
                            "moodScore", c.getMoodScore(),
                            "note", c.getNote() != null ? c.getNote() : ""
                    ))
                    .collect(Collectors.toList());
            export.put("checkIns", checkIns);
        } catch (Exception ignored) {}

        // Slutförda utmaningar
        try {
            var challenges = userChallengeRepository.findByUserIdAndCompleted(userId, true)
                    .stream()
                    .map(uc -> Map.of(
                            "challengeTitle", uc.getChallenge().getTitle(),
                            "category", uc.getChallenge().getCategory().name(),
                            "completedAt", uc.getCompletedAt() != null ? uc.getCompletedAt().toString() : "",
                            "pointsEarned", uc.getPointsEarned(),
                            "durationMinutes", uc.getActualMinutes()
                    ))
                    .collect(Collectors.toList());
            export.put("completedChallenges", challenges);
        } catch (Exception ignored) {}

        // Streak-data
        try {
            streakRepository.findByUserId(userId).ifPresent(streak -> {
                Map<String, Object> streakData = new LinkedHashMap<>();
                streakData.put("currentStreak", streak.getCurrentStreak());
                streakData.put("longestStreak", streak.getLongestStreak());
                streakData.put("lastActivityDate", streak.getLastActivityDate() != null ? streak.getLastActivityDate().toString() : null);
                export.put("streak", streakData);
            });
        } catch (Exception ignored) {}

        // Belöningar
        try {
            var rewards = rewardRepository.findByUserId(userId)
                    .stream()
                    .map(r -> Map.of(
                            "date", r.getRewardDate().toString(),
                            "type", r.getRewardType().name(),
                            "claimed", r.isClaimed()
                    ))
                    .collect(Collectors.toList());
            export.put("rewards", rewards);
        } catch (Exception ignored) {}

        // Samling
        try {
            var collectibles = collectibleRepository.findByUserId(userId)
                    .stream()
                    .map(c -> Map.of(
                            "key", c.getCollectibleKey(),
                            "type", c.getCollectibleType().name(),
                            "unlockedAt", c.getUnlockedAt().toString()
                    ))
                    .collect(Collectors.toList());
            export.put("collectibles", collectibles);
        } catch (Exception ignored) {}

        // Feed-data (publicerade inlägg + givna hejan)
        try {
            export.put("feedData", activityFeedService.exportFeedData(userId));
        } catch (Exception ignored) {}

        return export;
    }
}
