package se.sobriety.nextstep.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.entity.Challenge;
import se.sobriety.nextstep.entity.ChallengeCategory;
import se.sobriety.nextstep.entity.ChallengeDifficulty;
import se.sobriety.nextstep.repository.ChallengeRepository;
import se.sobriety.nextstep.repository.UserChallengeRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Service för att ladda challenges från challenges.json i databasen.
 * Nya challenges läggs till automatiskt vid uppstart baserat på titel-unikhet.
 * Filen challenges.json kan redigeras fritt utan omkompilering.
 */
@Service
public class ChallengeDataInitService {

    private static final Logger log = LoggerFactory.getLogger(ChallengeDataInitService.class);

    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;

    public ChallengeDataInitService(ChallengeRepository challengeRepository,
                                   UserChallengeRepository userChallengeRepository,
                                   EntityManager entityManager,
                                   ObjectMapper objectMapper) {
        this.challengeRepository = challengeRepository;
        this.userChallengeRepository = userChallengeRepository;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    @Transactional
    public void initializeChallenges() {
        // Ta bort gammal check constraint som blockerar nya enum-värden
        try {
            entityManager.createNativeQuery(
                    "ALTER TABLE challenge DROP CONSTRAINT IF EXISTS challenge_category_check"
            ).executeUpdate();
            log.info("Tog bort gammal challenge_category_check constraint");
        } catch (Exception e) {
            log.warn("Kunde inte ta bort challenge_category_check: {}", e.getMessage());
        }

        // Rensa trasig user_challenge-data från tidigare omstarter
        long userChallengeCount = userChallengeRepository.count();
        if (userChallengeCount > 0) {
            log.info("Rensar {} trasiga user_challenge-rader", userChallengeCount);
            userChallengeRepository.deleteAll();
        }

        // Läs challenges från JSON-filen
        List<Challenge> allChallenges = loadChallengesFromJson();
        if (allChallenges.isEmpty()) {
            log.warn("Inga challenges hittades i challenges.json");
            return;
        }

        // Om databasen är helt tom, lägg in alla challenges
        if (challengeRepository.count() == 0) {
            challengeRepository.saveAll(allChallenges);
            log.info("Skapade {} challenges i tom databas från challenges.json", allChallenges.size());
            return;
        }

        // Lägg till nya challenges som inte redan finns (baserat på titel)
        List<String> existingTitles = challengeRepository.findAll().stream()
                .map(Challenge::getTitle)
                .toList();

        List<Challenge> newChallenges = allChallenges.stream()
                .filter(c -> !existingTitles.contains(c.getTitle()))
                .toList();

        if (!newChallenges.isEmpty()) {
            challengeRepository.saveAll(newChallenges);
            log.info("Lade till {} nya challenges från challenges.json", newChallenges.size());
        }
    }

    /**
     * Läser challenges från src/main/resources/challenges.json
     */
    @SuppressWarnings("unchecked")
    private List<Challenge> loadChallengesFromJson() {
        try {
            InputStream inputStream = new ClassPathResource("challenges.json").getInputStream();
            List<Map<String, Object>> rawList = objectMapper.readValue(inputStream, new TypeReference<>() {});

            return rawList.stream()
                    .map(this::mapToChallenge)
                    .toList();
        } catch (IOException e) {
            log.error("Kunde inte läsa challenges.json: {}", e.getMessage());
            return List.of();
        }
    }

    private Challenge mapToChallenge(Map<String, Object> data) {
        String title = (String) data.get("title");
        String description = (String) data.get("description");
        int durationMinutes = (int) data.get("durationMinutes");
        ChallengeDifficulty difficulty = ChallengeDifficulty.valueOf((String) data.get("difficulty"));
        ChallengeCategory category = ChallengeCategory.valueOf((String) data.get("category"));
        String youtubeUrl = (String) data.get("youtubeUrl");
        String instructions = (String) data.get("instructions");

        Challenge challenge = new Challenge(title, description, durationMinutes, difficulty, category);
        challenge.setYoutubeUrl(youtubeUrl);
        challenge.setInstructions(instructions);
        return challenge;
    }
}
