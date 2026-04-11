package se.sobriety.nextstep.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import se.sobriety.nextstep.dto.UserSettingsInDto;
import se.sobriety.nextstep.dto.UserSettingsOutDto;
import se.sobriety.nextstep.entity.UserSettings;
import se.sobriety.nextstep.exception.UserNotFoundException;
import se.sobriety.nextstep.mapper.UserSettingsMapper;
import se.sobriety.nextstep.repository.UserSettingsRepository;

@Service
@Transactional
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;
    private final UserSettingsMapper userSettingsMapper;

    public UserSettingsService(
            UserSettingsRepository userSettingsRepository,
            UserSettingsMapper userSettingsMapper
    ) {
        this.userSettingsRepository = userSettingsRepository;
        this.userSettingsMapper = userSettingsMapper;
    }

    public UserSettingsOutDto getUserSettings(String userId) {
        UserSettings entity = userSettingsRepository
                .findByUserId(userId)
                .orElseGet(() -> createAndSaveDefault(userId));

        return userSettingsMapper.toDto(entity);
    }

    public UserSettingsOutDto updateUserSettings(String userId, UserSettingsInDto dto) {
        validateSettingsInput(dto);

        UserSettings entity = userSettingsRepository
                .findByUserId(userId)
                .orElseGet(() -> createAndSaveDefault(userId));

        applyUpdates(entity, dto);

        // Explicit save för garanterad persistering (dirty checking fungerar inte alltid
        // om entiteten skapades i ett separat anrop via createAndSaveDefault)
        userSettingsRepository.save(entity);
        return userSettingsMapper.toDto(entity);
    }

    public void deleteUserSettings(String userId) {
        UserSettings entity = userSettingsRepository
                .findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User settings not found"));

        userSettingsRepository.delete(entity);
    }

    /* ===================== PRIVATE ===================== */

    private void validateSettingsInput(UserSettingsInDto dto) {
        if (dto.email() != null && !dto.email().isBlank() && !dto.email().contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private void applyUpdates(UserSettings entity, UserSettingsInDto dto) {
        entity.setName(dto.name());
        entity.setEmail(dto.email());
        entity.setPhone(dto.phone());
        entity.setNotificationsEnabled(dto.notificationsEnabled());
        entity.setAiNotificationsEnabled(dto.aiNotificationsEnabled());
        entity.setDarkModeEnabled(dto.darkModeEnabled());
        entity.setLanguage(dto.language());
        if (dto.coachPersonality() != null) {
            entity.setCoachPersonality(dto.coachPersonality());
        }
        // Feed-delningsinställningar (opt-in)
        if (dto.feedSharingEnabled() != null) {
            entity.setFeedSharingEnabled(dto.feedSharingEnabled());
        }
        if (dto.feedSharedTypes() != null) {
            entity.getFeedSharedTypes().clear();
            entity.getFeedSharedTypes().addAll(dto.feedSharedTypes());
        }
    }

    private UserSettings createAndSaveDefault(String userId) {
        // ✅ enda tillåtna sättet att skapa
        return userSettingsRepository.save(new UserSettings(userId));
    }

    public UserSettings getOrCreateSettings(String userId) {
        return userSettingsRepository
                .findByUserId(userId)
                .orElseGet(() -> createAndSaveDefault(userId));
    }

}
