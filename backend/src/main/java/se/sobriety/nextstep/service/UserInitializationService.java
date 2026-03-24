package se.sobriety.nextstep.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import se.sobriety.nextstep.entity.UserSettings;

@Service
@Transactional
public class UserInitializationService {

    private final UserProgressService userProgressService;
    private final UserSettingsService userSettingsService;
    private final StreakService streakService;

    public UserInitializationService(
            UserProgressService userProgressService,
            UserSettingsService userSettingsService,
            StreakService streakService
    ) {
        this.userProgressService = userProgressService;
        this.userSettingsService = userSettingsService;
        this.streakService = streakService;
    }

    @Transactional
    public void ensureUserExistsAndUpdateFromAuth(String userId, String name, String email) {
        // Hämta eller skapa UserProgress
        userProgressService.getOrCreateUser(userId);

        // Hämta eller skapa UserStreak
        streakService.getOrCreateStreak(userId);

        // Hämta eller skapa UserSettings
        UserSettings settings = userSettingsService.getOrCreateSettings(userId);

        if (name != null && !name.equals(settings.getName())) {
            settings.setName(name);
        }
        if (email != null && !email.equals(settings.getEmail())) {
            settings.setEmail(email);
        }
    }

}

