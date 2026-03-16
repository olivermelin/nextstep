package se.sobriety.nextstep.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.dto.*;
import se.sobriety.nextstep.entity.*;
import se.sobriety.nextstep.exception.UserNotFoundException;
import se.sobriety.nextstep.repository.*;
import se.sobriety.nextstep.service.mail.MailType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional(readOnly = true)
public class UserProgressService {

    private final UserProgressRepository progressRepository;
    private final UserSettingsRepository settingsRepository;
    private final LevelService levelService;
    private final MailService mailService;
    private final CategoryProgressRepository categoryProgressRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;

    @Autowired
    public UserProgressService(UserProgressRepository repo,
                              UserSettingsRepository userSettingsRepository,
                              LevelService levelService,
                              MailService mailService,
                              CategoryProgressRepository categoryProgressRepository,
                              UserChallengeRepository userChallengeRepository,
                              AchievementRepository achievementRepository,
                              UserAchievementRepository userAchievementRepository) {
        this.progressRepository = repo;
        this.settingsRepository = userSettingsRepository;
        this.levelService = levelService;
        this.mailService = mailService;
        this.categoryProgressRepository = categoryProgressRepository;
        this.userChallengeRepository = userChallengeRepository;
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
    }

    /* =========================
       Hämta / skapa
       ========================= */

    public UserProgress getUser(String userId) {
        return progressRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional
    public UserProgress getOrCreateUser(String userId) {
        return progressRepository.findByUserId(userId)
                .orElseGet(() -> progressRepository.save(new UserProgress(userId)));
    }


    @Transactional
    public UserProgress createUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }

        if (progressRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("User already exists: " + userId);
        }

        return progressRepository.save(new UserProgress(userId));
    }

    @Transactional
    public void deleteUser(String userId) {
        UserProgress user = getUser(userId);
        progressRepository.delete(user);
    }

    @Transactional
    public UserProgress resetProgress(String userId) {
        UserProgress user = getUser(userId);

        // ny instans → ren state
        UserProgress reset = new UserProgress(user.getUserId());
        reset = progressRepository.save(reset);

        progressRepository.delete(user);
        return reset;
    }


    @Transactional
    public UserProgress addPoints(String userId, int points) {
        UserProgress user = getUser(userId);
        int previousLevel = user.getLevel();
        user.applyPoints(points);
        int newLevel = levelService.calculateLevel(user.getTotalPoints());
        user.updateLevel(newLevel);

        // Skicka level-up mail om nivå ökades
        if (newLevel > previousLevel) {
            UserSettings userSettings = settingsRepository.findByUserId(user.getUserId())
                    .orElseThrow(() -> new UserNotFoundException(user.getUserId()));

            sendLevelUpEmail(userSettings, user.getUserId(), newLevel);
        }

        return progressRepository.save(user);
    }

    /**
     * Skicka level-up email till användare
     */
    private void sendLevelUpEmail(UserSettings userSettings, String userId, int newLevel) {
        try {
            MailRequest mailRequest = MailRequest.builder()
                    .type(MailType.LEVEL_UP)
                    .recipientEmail(userSettings.getEmail())
                    .build();

            mailRequest.addVariable("firstName", userSettings.getName() != null ? userSettings.getName() : "Användare");
            mailRequest.addVariable("level", String.valueOf(newLevel));

            mailService.sendMail(mailRequest);
        } catch (Exception e) {
            // Log-varning men fortsätt - mail-fel ska inte stoppa progress-uppdatering
            log.warn("Kunde inte skicka level-up mail till användare {}: {}", userSettings.getUserId(), e.getMessage());
        }
    }

    /* =========================
       Nya metoder för progresshantering
       ========================= */

    /**
     * Hämta total framsteg för användare
     */
    public ProgressResponseDto getTotalProgress(String userId) {
        UserProgress user = getUser(userId);
        return new ProgressResponseDto(
                user.getUserId(),
                user.getTotalPoints(),
                user.getLevel(),
                user.getLastUpdated().toString()
        );
    }

    /**
     * Hämta framsteg per kategori för en viss period (baserat på completed challenges)
     */
    public CategoryProgressResponseDto getCategoryProgress(String userId, int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);

        // Hämta alla completed challenges för användaren
        List<UserChallenge> completedChallenges = userChallengeRepository
                .findByUserIdAndCompleted(userId, true)
                .stream()
                .filter(uc -> uc.getCompletedAt() != null && uc.getCompletedAt().isAfter(cutoffDate))
                .collect(Collectors.toList());

        // Räkna poäng per kategori
        List<CategoryProgressDto> categoryProgress = new ArrayList<>();

        for (ChallengeCategory category : ChallengeCategory.values()) {
            int points = completedChallenges.stream()
                    .filter(uc -> uc.getChallenge().getCategory() == category)
                    .mapToInt(UserChallenge::getPointsEarned)
                    .sum();

            categoryProgress.add(new CategoryProgressDto(getCategoryId(category), points));
        }

        return new CategoryProgressResponseDto(userId, days, categoryProgress);
    }

    /**
     * Hämta användarens achievements
     */
    public List<AchievementDto> getUserAchievements(String userId) {
        UserProgress user = getUser(userId);
        List<Achievement> allAchievements = achievementRepository.findAll();

        return allAchievements.stream()
                .map(achievement -> {
                    boolean unlocked = user.getTotalPoints() >= achievement.getRequiredPoints()
                            && user.getLevel() >= achievement.getRequiredLevel();

                    return new AchievementDto(
                            achievement.getAchievementId(),
                            achievement.getTitle(),
                            achievement.getDescription(),
                            unlocked,
                            achievement.getRequiredPoints(),
                            achievement.getRequiredLevel()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Lägg till poäng och uppdatera kategori-framsteg
     */
    @Transactional
    public ProgressResponseDto addPointsWithCategory(String userId, int points, ChallengeCategory category) {
        // Uppdatera total progress
        UserProgress user = addPoints(userId, points);

        // Uppdatera kategori-progress
        CategoryProgress categoryProgress = categoryProgressRepository
                .findByUserIdAndCategory(userId, category)
                .orElse(new CategoryProgress(userId, category));

        categoryProgress.addPoints(points);
        categoryProgressRepository.save(categoryProgress);

        return new ProgressResponseDto(
                user.getUserId(),
                user.getTotalPoints(),
                user.getLevel(),
                user.getLastUpdated().toString()
        );
    }

    private String getCategoryId(ChallengeCategory category) {
        return category.getId();
    }
}
