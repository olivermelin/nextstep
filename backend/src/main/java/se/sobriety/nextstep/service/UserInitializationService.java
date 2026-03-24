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

        // Sätt namn och e-post enbart om de fortfarande har defaultvärden.
        // Vi skriver ALDRIG över ett namn/e-post som användaren aktivt har ändrat i inställningarna.
        boolean nameIsDefault = settings.getName() == null
                || settings.getName().isBlank()
                || settings.getName().equals("Default Name");
        if (nameIsDefault && name != null && !name.isBlank()) {
            settings.setName(name);
        }

        boolean emailIsDefault = settings.getEmail() == null || settings.getEmail().isBlank();
        if (emailIsDefault && email != null && !email.isBlank()) {
            settings.setEmail(email);
        }
    }

}

