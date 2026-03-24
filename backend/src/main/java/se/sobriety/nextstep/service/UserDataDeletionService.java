package se.sobriety.nextstep.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.repository.*;
import se.sobriety.nextstep.service.QuotaService;

/**
 * GDPR — Rätt till radering (artikel 17 GDPR).
 * Raderar ALLA personuppgifter för en användare i en transaktion.
 *
 * Ordning är viktig: relations-data raderas före huvud-entiteter.
 * CoachSession cascadar CoachMessage via CascadeType.ALL.
 */
@Slf4j
@Service
public class UserDataDeletionService {

    private final UserProgressRepository progressRepository;
    private final UserSettingsRepository settingsRepository;
    private final CategoryProgressRepository categoryProgressRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final CoachSessionRepository coachSessionRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserStreakRepository userStreakRepository;
    private final DailyCheckInRepository dailyCheckInRepository;
    private final UserRepository userRepository;
    private final QuotaService quotaService;

    public UserDataDeletionService(
            UserProgressRepository progressRepository,
            UserSettingsRepository settingsRepository,
            CategoryProgressRepository categoryProgressRepository,
            UserChallengeRepository userChallengeRepository,
            CoachSessionRepository coachSessionRepository,
            UserAchievementRepository userAchievementRepository,
            UserStreakRepository userStreakRepository,
            DailyCheckInRepository dailyCheckInRepository,
            UserRepository userRepository,
            QuotaService quotaService) {
        this.progressRepository = progressRepository;
        this.settingsRepository = settingsRepository;
        this.categoryProgressRepository = categoryProgressRepository;
        this.userChallengeRepository = userChallengeRepository;
        this.coachSessionRepository = coachSessionRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.userStreakRepository = userStreakRepository;
        this.dailyCheckInRepository = dailyCheckInRepository;
        this.userRepository = userRepository;
        this.quotaService = quotaService;
    }

    /**
     * Raderar all data för en användare (GDPR artikel 17).
     *
     * Inkluderar: AI-konversationer, check-ins, streak, utmaningar,
     * framsteg, kategoriprogress, achievements, inställningar och kontot.
     *
     * @param userId userId (email) för den användare som ska raderas
     */
    @Transactional
    public void deleteAllUserData(String userId) {
        log.info("GDPR-radering initierad för användare: {}", userId);

        // AI-sessioner (cascadar CoachMessage automatiskt)
        coachSessionRepository.deleteByUserId(userId);

        // Dagliga incheckningar
        dailyCheckInRepository.deleteByUserId(userId);

        // Meddelandekvoter
        quotaService.deleteAllForUser(userId);

        // Streak-data
        userStreakRepository.deleteByUserId(userId);

        // Utmanings-historik
        userChallengeRepository.deleteByUserId(userId);

        // Achievements
        userAchievementRepository.deleteByUserId(userId);

        // Kategori-framsteg
        categoryProgressRepository.deleteByUserId(userId);

        // Total progress
        progressRepository.deleteByUserId(userId);

        // Inställningar och onboarding-data (inkl. substanshistorik, BackgroundInfo)
        settingsRepository.deleteByUserId(userId);

        // Användarkontot
        userRepository.findByEmail(userId).ifPresent(userRepository::delete);

        log.info("GDPR-radering slutförd för användare: {}", userId);
    }
}
