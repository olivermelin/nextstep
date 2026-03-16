package se.sobriety.nextstep.service.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.sobriety.nextstep.entity.Challenge;
import se.sobriety.nextstep.entity.ChallengeCategory;
import se.sobriety.nextstep.entity.ChallengeDifficulty;
import se.sobriety.nextstep.repository.ChallengeRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChallengeRecommendationParserTest {

    @Mock
    private ChallengeRepository challengeRepository;

    private ChallengeRecommendationParser parser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        parser = new ChallengeRecommendationParser(challengeRepository);
    }

    @Test
    void parse_nullInput_returnsEmptyResult() {
        var result = parser.parse(null);
        assertNull(result.cleanedResponse());
        assertTrue(result.suggestedChallenges().isEmpty());
    }

    @Test
    void parse_blankInput_returnsEmptyResult() {
        var result = parser.parse("  ");
        assertTrue(result.suggestedChallenges().isEmpty());
    }

    @Test
    void parse_noTags_returnsOriginalText() {
        String text = "Det låter bra att du vill prova något nytt!";
        var result = parser.parse(text);

        assertEquals(text, result.cleanedResponse());
        assertTrue(result.suggestedChallenges().isEmpty());
    }

    @Test
    void parse_singleTag_extractsAndCleansResponse() {
        Challenge challenge = new Challenge("Promenad", "Ta en promenad", 30,
                ChallengeDifficulty.EASY, ChallengeCategory.PHYSICAL_ACTIVITY);
        challenge.setId(5L);
        when(challengeRepository.findById(5L)).thenReturn(Optional.of(challenge));

        String text = "Jag rekommenderar en promenad! [CHALLENGE:5] Försök ta det lugnt.";
        var result = parser.parse(text);

        assertFalse(result.cleanedResponse().contains("[CHALLENGE:5]"));
        assertTrue(result.cleanedResponse().contains("Jag rekommenderar"));
        assertTrue(result.cleanedResponse().contains("Försök ta det lugnt"));
        assertEquals(1, result.suggestedChallenges().size());
        assertEquals("Promenad", result.suggestedChallenges().getFirst().title());
        assertEquals("/challenges/physical-activity/5", result.suggestedChallenges().getFirst().url());
    }

    @Test
    void parse_multipleTags_extractsAll() {
        Challenge c1 = new Challenge("Meditation", "Meditera", 10,
                ChallengeDifficulty.EASY, ChallengeCategory.MENTAL_HEALTH);
        c1.setId(1L);
        Challenge c2 = new Challenge("Löpning", "Spring", 30,
                ChallengeDifficulty.MEDIUM, ChallengeCategory.PHYSICAL_ACTIVITY);
        c2.setId(2L);
        when(challengeRepository.findById(1L)).thenReturn(Optional.of(c1));
        when(challengeRepository.findById(2L)).thenReturn(Optional.of(c2));

        String text = "Prova [CHALLENGE:1] och sedan [CHALLENGE:2] för bästa effekt.";
        var result = parser.parse(text);

        assertEquals(2, result.suggestedChallenges().size());
        assertEquals("Meditation", result.suggestedChallenges().get(0).title());
        assertEquals("Löpning", result.suggestedChallenges().get(1).title());
    }

    @Test
    void parse_duplicateTags_onlyReturnsUnique() {
        Challenge c1 = new Challenge("Meditation", "Meditera", 10,
                ChallengeDifficulty.EASY, ChallengeCategory.MENTAL_HEALTH);
        c1.setId(1L);
        when(challengeRepository.findById(1L)).thenReturn(Optional.of(c1));

        String text = "Prova [CHALLENGE:1] igen [CHALLENGE:1]!";
        var result = parser.parse(text);

        assertEquals(1, result.suggestedChallenges().size());
    }

    @Test
    void parse_nonexistentChallenge_skipsGracefully() {
        when(challengeRepository.findById(999L)).thenReturn(Optional.empty());

        String text = "Prova [CHALLENGE:999]!";
        var result = parser.parse(text);

        assertTrue(result.suggestedChallenges().isEmpty());
    }

    @Test
    void parse_frontendUrl_correctForAllCategories() {
        // Test a few different categories for correct URL slugs
        Challenge drawing = new Challenge("Rita", "Ritövning", 15,
                ChallengeDifficulty.EASY, ChallengeCategory.DRAWING_EXERCISES);
        drawing.setId(10L);
        when(challengeRepository.findById(10L)).thenReturn(Optional.of(drawing));

        var result = parser.parse("Prova [CHALLENGE:10]");
        assertEquals("/challenges/drawing-exercises/10", result.suggestedChallenges().getFirst().url());
    }

    @Test
    void parse_cleanedResponse_removesExtraWhitespace() {
        when(challengeRepository.findById(1L)).thenReturn(Optional.empty());

        String text = "Text  [CHALLENGE:1]  more text";
        var result = parser.parse(text);

        // Should not have double spaces after tag removal
        assertFalse(result.cleanedResponse().contains("  "));
    }
}
