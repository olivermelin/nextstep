package se.sobriety.nextstep.dto;

/**
 * Vännepost i vänskaplistan.
 * Exponerar INTE e-post — bara visningsnamn och XP.
 */
public record FriendDto(
        Long friendshipId,
        String displayName,
        int totalPoints,
        String status,           // PENDING / ACCEPTED
        boolean iRequested,      // true om jag skickade förfrågan
        String friendshipSince   // ISO-datum
) {}
