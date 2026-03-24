package se.sobriety.nextstep.dto;

public record DailyRewardDto(
        Long id,
        String rewardDate,
        String rewardType,
        String rewardValue,
        boolean claimed,
        int consecutiveDays,
        boolean rare
) {}
