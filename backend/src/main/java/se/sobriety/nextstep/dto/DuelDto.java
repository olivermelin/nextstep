package se.sobriety.nextstep.dto;

/**
 * Fullständig representation av en duell.
 */
public record DuelDto(
        Long id,
        String challengerName,
        String challengedName,
        Long challengeId,
        String challengeName,
        String status,
        String winnerId,       // null tills avgjord
        String winnerName,     // null tills avgjord
        boolean iAmChallenger,
        boolean iCompleted,
        boolean opponentCompleted,
        String createdAt,
        String expiresAt,
        String completedAt
) {}
