package se.sobriety.nextstep.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import se.sobriety.nextstep.entity.UserSettings;
import se.sobriety.nextstep.repository.UserSettingsRepository;

@Service
@Transactional
public class UserInitializationService {

    private final UserProgressService userProgressService;
    private final UserSettingsService userSettingsService;
    private final UserSettingsRepository userSettingsRepository;

    public UserInitializationService(
            UserProgressService userProgressService,
            UserSettingsService userSettingsService,
            UserSettingsRepository userSettingsRepository
    ) {
        this.userProgressService = userProgressService;
        this.userSettingsService = userSettingsService;
        this.userSettingsRepository = userSettingsRepository;
    }

    /**
     * Säkerställer att användaren finns och uppdaterar namn/email.
     * Returnerar true om användaren precis skapades (ny användare), annars false.
     */
    @Transactional
    public boolean ensureUserExistsAndUpdateFromAuth(String userId, String name, String email) {
        // Kolla om användaren redan finns innan vi skapar
        boolean isNewUser = userSettingsRepository.findByUserId(userId).isEmpty();

        // Hämta eller skapa UserProgress
        userProgressService.getOrCreateUser(userId);

        // Hämta eller skapa UserSettings
        UserSettings settings = userSettingsService.getOrCreateSettings(userId);

        if (name != null && !name.equals(settings.getName())) {
            settings.setName(name);
        }
        if (email != null && !email.equals(settings.getEmail())) {
            settings.setEmail(email);
        }

        return isNewUser;
    }

}

