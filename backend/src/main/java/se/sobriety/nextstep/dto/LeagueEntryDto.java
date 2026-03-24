package se.sobriety.nextstep.dto;

/**
 * Post i ligalistan. E-post exponeras INTE — bara visningsnamn och poäng.
 */
public record LeagueEntryDto(
        int rank,
        String displayName,
        int totalPoints,
        String tier,      // BRONZE / SILVER / GOLD / DIAMOND
        boolean isMe
) {}
