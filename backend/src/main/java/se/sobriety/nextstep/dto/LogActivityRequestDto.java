package se.sobriety.nextstep.dto;

public record LogActivityRequestDto(
        String activityType,
        String metadata
) {}
