package se.sobriety.nextstep.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import se.sobriety.nextstep.entity.Achievement;
import se.sobriety.nextstep.repository.AchievementRepository;

/**
 * Service för att ladda initiala achievements i databasen
 */
@Service
public class AchievementDataInitService {

    private final AchievementRepository achievementRepository;

    public AchievementDataInitService(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    @PostConstruct
    @Transactional
    public void initializeAchievements() {
        if (achievementRepository.count() > 0) {
            return; // Achievements redan initialiserade
        }

        Achievement[] achievements = {
                new Achievement(1, "First Steps", "Earn your first 15 points", 15, 1),
                new Achievement(2, "Getting Started", "Reach level 2", 100, 2),
                new Achievement(3, "Dedicated", "Earn 250 points", 250, 3),
                new Achievement(4, "Committed", "Reach level 5", 400, 5),
                new Achievement(5, "Champion", "Earn 1000 points", 1000, 10),
                new Achievement(6, "Master", "Reach level 15", 1400, 15)
        };

        for (Achievement achievement : achievements) {
            achievementRepository.save(achievement);
        }
    }
}

