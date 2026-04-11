package se.sobriety.nextstep.dto;

import se.sobriety.nextstep.entity.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record UserSettingsOutDto(
        String userId,
        String name,
        String email,
        String phone,
        boolean notificationsEnabled,
        boolean aiNotificationsEnabled,
        boolean darkModeEnabled,
        String language,
        Boolean onboardingCompleted,
        List<UserGoal> userGoals,
        String otherGoal,
        RecoveryStage recoveryStage,
        BackgroundInfoDto backgroundInfo,
        LocalDateTime lastUpdated,
        SubscriptionTier subscriptionTier,
        CoachPersonality coachPersonality,
        boolean feedSharingEnabled,
        Set<FeedItemType> feedSharedTypes
) {}
