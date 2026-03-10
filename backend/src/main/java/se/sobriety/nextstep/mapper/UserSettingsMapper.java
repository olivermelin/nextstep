package se.sobriety.nextstep.mapper;

import org.springframework.stereotype.Component;
import se.sobriety.nextstep.dto.BackgroundInfoDto;
import se.sobriety.nextstep.dto.UserSettingsOutDto;
import se.sobriety.nextstep.entity.BackgroundInfo;
import se.sobriety.nextstep.entity.UserSettings;

@Component
public class UserSettingsMapper {

    public UserSettingsOutDto toDto(UserSettings entity) {
        // Konvertera BackgroundInfo till DTO om den finns
        BackgroundInfoDto backgroundInfoDto = null;
        if (entity.getBackgroundInfo() != null) {
            BackgroundInfo bg = entity.getBackgroundInfo();
            backgroundInfoDto = new BackgroundInfoDto(
                    bg.getAge(),
                    bg.getGender(),
                    bg.getCurrentSituation(),
                    bg.getSubstanceHistory()
            );
        }

        return new UserSettingsOutDto(
                entity.getUserId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.isNotificationsEnabled(),
                entity.isAiNotificationsEnabled(),
                entity.isDarkModeEnabled(),
                entity.getLanguage(),
                entity.getOnboardingCompleted(),
                entity.getUserGoals(),
                entity.getOtherGoal(),
                entity.getRecoveryStage(),
                backgroundInfoDto,
                entity.getLastUpdated()
        );
    }
}
