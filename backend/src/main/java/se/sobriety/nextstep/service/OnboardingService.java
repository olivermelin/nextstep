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

    private static final int MAX_TEXT_FIELD_LENGTH = 1000;

    /**
     * Slutför onboarding och spara användarens val
     */
    public void completeOnboarding(String userId, OnboardingDataDto data) {
        validateOnboardingInput(userId, data);

        log.info("completeOnboarding called for userId: '{}'", userId);

        // Använd getOrCreateSettings för att undvika duplicerade rader
        UserSettings userSettings = userSettingsService.getOrCreateSettings(userId);
        log.info("UserSettings found/created with id={}, userId='{}', onboardingCompleted={}",
                userSettings.getId(), userSettings.getUserId(), userSettings.getOnboardingCompleted());

        // Spara onboarding-spår (konsument eller återhämtning)
        userSettings.setOnboardingTrack(data.onboardingTrack());

        // Sätt användarens mål
        userSettings.setUserGoals(data.userGoals());
        userSettings.setOtherGoal(data.otherGoal());

        // Recovery stage och substanshistorik sparas bara vid RECOVERY-spåret
        if (data.recoveryStage() != null) {
            userSettings.setRecoveryStage(data.recoveryStage());
        }

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

        // Spara samtycke till AI-beteendeanalys
        boolean consent = Boolean.TRUE.equals(data.aiAnalysisConsent());
        userSettings.setAiAnalysisConsent(consent);
        userSettings.setAiAnalysisConsentAt(consent ? java.time.LocalDateTime.now() : null);

        // Markera onboarding som slutförd
        userSettings.setOnboardingCompleted(true);

        UserSettings saved = userSettingsRepository.save(userSettings);
        log.info("Onboarding saved for userId='{}', onboardingCompleted={}", saved.getUserId(), saved.getOnboardingCompleted());
    }

    /**
     * Hämta användarens onboarding-spår (CONSUMER eller RECOVERY), eller null om ej satt
     */
    @Transactional(readOnly = true)
    public String getOnboardingTrack(String userId) {
        if (userId == null || userId.isBlank()) return null;

        var rows = userSettingsRepository.findAllByUserId(userId);
        var found = rows.stream()
                .filter(s -> s.getOnboardingTrack() != null)
                .findFirst();
        if (found.isPresent()) return found.get().getOnboardingTrack().name();

        var byEmail = userSettingsRepository.findAllByEmail(userId);
        return byEmail.stream()
                .filter(s -> s.getOnboardingTrack() != null)
                .findFirst()
                .map(s -> s.getOnboardingTrack().name())
                .orElse(null);
    }

    /**
     * Kontrollera om användaren har slutfört onboarding
     */
    @Transactional(readOnly = true)
    public boolean isOnboardingCompleted(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be null or blank");
        }

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

    /* ===================== PRIVATE ===================== */

    private void validateOnboardingInput(String userId, OnboardingDataDto data) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be null or blank");
        }
        if (data == null) {
            throw new IllegalArgumentException("Onboarding data must not be null");
        }
        if (data.userGoals() == null || data.userGoals().isEmpty()) {
            throw new IllegalArgumentException("User goals are required");
        }
        // recoveryStage är valfritt — användaren kan hoppa över steg 3
        if (data.otherGoal() != null && data.otherGoal().length() > MAX_TEXT_FIELD_LENGTH) {
            throw new IllegalArgumentException("Other goal text exceeds maximum length of " + MAX_TEXT_FIELD_LENGTH);
        }
    }
}

