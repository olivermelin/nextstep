package se.sobriety.nextstep.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.dto.BackgroundInfoDto;
import se.sobriety.nextstep.dto.OnboardingDataDto;
import se.sobriety.nextstep.entity.BackgroundInfo;
import se.sobriety.nextstep.entity.UserSettings;
import se.sobriety.nextstep.repository.UserSettingsRepository;

/**
 * Service för att hantera onboarding-processen
 */
@Service
@Transactional
public class OnboardingService {

    private static final Logger log = LoggerFactory.getLogger(OnboardingService.class);

    private final UserSettingsRepository userSettingsRepository;
    private final UserSettingsService userSettingsService;

    public OnboardingService(UserSettingsRepository userSettingsRepository,
                            UserSettingsService userSettingsService) {
        this.userSettingsRepository = userSettingsRepository;
        this.userSettingsService = userSettingsService;
    }

    /**
     * Slutför onboarding och spara användarens val
     */
    public void completeOnboarding(String userId, OnboardingDataDto data) {
        log.info("completeOnboarding called for userId: '{}'", userId);

        // Använd getOrCreateSettings för att undvika duplicerade rader
        UserSettings userSettings = userSettingsService.getOrCreateSettings(userId);
        log.info("UserSettings found/created with id={}, userId='{}', onboardingCompleted={}",
                userSettings.getId(), userSettings.getUserId(), userSettings.getOnboardingCompleted());

        // Sätt användarens mål
        userSettings.setUserGoals(data.userGoals());
        userSettings.setOtherGoal(data.otherGoal());
        userSettings.setRecoveryStage(data.recoveryStage());

        // Konvertera och sätt bakgrundsinformation
        if (data.backgroundInfo() != null) {
            BackgroundInfoDto bgDto = data.backgroundInfo();
            BackgroundInfo backgroundInfo = new BackgroundInfo(
                    bgDto.age(),
                    bgDto.gender(),
                    bgDto.currentSituation(),
                    bgDto.substanceHistory()
            );
            userSettings.setBackgroundInfo(backgroundInfo);
        }

        // Markera onboarding som slutförd
        userSettings.setOnboardingCompleted(true);

        UserSettings saved = userSettingsRepository.save(userSettings);
        log.info("Onboarding saved for userId='{}', onboardingCompleted={}", saved.getUserId(), saved.getOnboardingCompleted());
    }

    /**
     * Kontrollera om användaren har slutfört onboarding
     */
    @Transactional(readOnly = true)
    public boolean isOnboardingCompleted(String userId) {
        // Kolla alla möjliga UserSettings-rader (hanterar eventuella duplikat och userId/email mismatch)
        var allByUserId = userSettingsRepository.findAllByUserId(userId);
        boolean byUserId = allByUserId.stream()
                .anyMatch(s -> Boolean.TRUE.equals(s.getOnboardingCompleted()));

        if (byUserId) {
            log.info("isOnboardingCompleted('{}') = true (found by userId)", userId);
            return true;
        }

        // Fallback: kolla om onboarding gjorts via email (hanterar migration från sub → email)
        var allByEmail = userSettingsRepository.findAllByEmail(userId);
        boolean byEmail = allByEmail.stream()
                .anyMatch(s -> Boolean.TRUE.equals(s.getOnboardingCompleted()));

        log.info("isOnboardingCompleted('{}') = {} (byUserId={}, byEmail={}, rows: userId={}, email={})",
                userId, byEmail, false, byEmail, allByUserId.size(), allByEmail.size());
        return byEmail;
    }
}

