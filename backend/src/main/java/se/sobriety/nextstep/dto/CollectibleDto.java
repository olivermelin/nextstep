package se.sobriety.nextstep.dto;

public record CollectibleDto(
        String collectibleKey,
        String collectibleType,
        String unlockedAt
) {}
