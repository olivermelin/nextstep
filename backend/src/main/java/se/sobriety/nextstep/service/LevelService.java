package se.sobriety.nextstep.service;

import org.springframework.stereotype.Service;

@Service
public class LevelService {

    private static final int BASE_XP = 100;
    private static final int XP_INCREMENT = 50;

    public int calculateLevel(long totalXp) {
        int level = 1;
        long xpRemaining = totalXp;
        int xpToNextLevel = BASE_XP;

        while (xpRemaining >= xpToNextLevel) {
            xpRemaining -= xpToNextLevel;
            level++;
            xpToNextLevel += XP_INCREMENT;
        }

        return level;
    }
}
