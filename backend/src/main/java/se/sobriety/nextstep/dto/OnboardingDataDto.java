package se.sobriety.nextstep.dto;

import se.sobriety.nextstep.entity.*;

import java.util.List;

/**
 * DTO för onboarding-data från frontend
 */
public record OnboardingDataDto(
        List<UserGoal> userGoals,
        String otherGoal,
        BackgroundInfoDto backgroundInfo,
        RecoveryStage recoveryStage
) {}

