package se.sobriety.nextstep.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import se.sobriety.nextstep.entity.*;

import java.util.List;

/**
 * DTO för onboarding-data från frontend
 */
public record OnboardingDataDto(
        @NotNull(message = "User goals are required")
        @Size(min = 1, max = 10, message = "Must select between 1 and 10 goals")
        List<UserGoal> userGoals,

        @Size(max = 500, message = "Other goal must be at most 500 characters")
        String otherGoal,

        @Valid
        BackgroundInfoDto backgroundInfo,

        RecoveryStage recoveryStage
) {}
