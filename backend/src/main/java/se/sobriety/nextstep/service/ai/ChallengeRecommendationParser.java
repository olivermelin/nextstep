package se.sobriety.nextstep.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sobriety.nextstep.dto.SuggestedChallengeDto;
import se.sobriety.nextstep.entity.Challenge;
import se.sobriety.nextstep.entity.ChallengeCategory;
import se.sobriety.nextstep.repository.ChallengeRepository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsar AI-svar och extraherar challenge-rekommendationer via [CHALLENGE:id]-taggar.
 * Returnerar rensat svar + en lista med SuggestedChallengeDto.
 */
@Component
public class ChallengeRecommendationParser {

    private static final Logger log = LoggerFactory.getLogger(ChallengeRecommendationParser.class);
    // Matchar [CHALLENGE:1], [CHALLENGE: 1] och [CHALLENGE : 1] (AI-modeller kan variera formatet)
    private static final Pattern CHALLENGE_TAG_PATTERN = Pattern.compile("\\[CHALLENGE\\s*:\\s*(\\d+)\\s*]");

    private final ChallengeRepository challengeRepository;

    public ChallengeRecommendationParser(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    /**
     * Resultat med rensat svar och eventuella challenge-rekommendationer.
     */
    public record ParseResult(String cleanedResponse, List<SuggestedChallengeDto> suggestedChallenges) {}

    /**
     * Parsar AI-svaret: extraherar [CHALLENGE:id]-taggar, slår upp i databasen,
     * och returnerar rensat svar + lista med rekommenderade challenges.
     */
    public ParseResult parse(String aiResponse) {
        if (aiResponse == null || aiResponse.isBlank()) {
            return new ParseResult(aiResponse, List.of());
        }

        Matcher matcher = CHALLENGE_TAG_PATTERN.matcher(aiResponse);

        // Samla unika ID:n i insättningsordning
        Set<Long> challengeIds = new LinkedHashSet<>();
        while (matcher.find()) {
            try {
                challengeIds.add(Long.parseLong(matcher.group(1)));
            } catch (NumberFormatException e) {
                log.warn("Could not parse challenge ID from tag: {}", matcher.group());
            }
        }

        // Ta bort taggarna ur svaret
        String cleanedResponse = CHALLENGE_TAG_PATTERN.matcher(aiResponse).replaceAll("").trim();
        // Ta bort eventuella dubbla mellanslag som uppstår
        cleanedResponse = cleanedResponse.replaceAll(" {2,}", " ");
        // Ta bort tomma rader som kan uppstå
        cleanedResponse = cleanedResponse.replaceAll("(?m)^\\s*$\\n?", "");

        if (challengeIds.isEmpty()) {
            return new ParseResult(cleanedResponse, List.of());
        }

        // Slå upp challenges i databasen
        List<SuggestedChallengeDto> suggestions = new ArrayList<>();
        for (Long id : challengeIds) {
            Optional<Challenge> challengeOpt = challengeRepository.findById(id);
            if (challengeOpt.isPresent()) {
                Challenge c = challengeOpt.get();
                suggestions.add(new SuggestedChallengeDto(
                        c.getId(),
                        c.getTitle(),
                        c.getDescription(),
                        c.getDurationMinutes(),
                        c.getDifficulty().name(),
                        c.getCategory().name(),
                        buildFrontendUrl(c.getCategory(), c.getId())
                ));
            } else {
                log.warn("AI recommended non-existent challenge ID: {}", id);
            }
        }

        log.info("Parsed {} challenge recommendations from AI response", suggestions.size());
        return new ParseResult(cleanedResponse, suggestions);
    }

    /**
     * Bygger frontend-URL för en challenge, t.ex. /challenges/physical-activity/5
     */
    private String buildFrontendUrl(ChallengeCategory category, Long challengeId) {
        String slug = switch (category) {
            case MENTAL_HEALTH -> "mental-health";
            case PHYSICAL_ACTIVITY -> "physical-activity";
            case FOCUS_DISCIPLINE -> "focus-discipline";
            case PERSONAL_DEVELOPMENT -> "personal-development";
            case DRAWING_EXERCISES -> "drawing-exercises";
            case HEALTHY_HABITS -> "healthy-habits";
            case SOCIAL_SKILLS -> "social-skills";
            case EMOTIONAL_AWARENESS -> "emotional-awareness";
        };
        return "/challenges/" + slug + "/" + challengeId;
    }
}

