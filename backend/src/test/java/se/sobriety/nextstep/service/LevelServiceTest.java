package se.sobriety.nextstep.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LevelServiceTest {

    private LevelService levelService;

    @BeforeEach
    void setUp() {
        levelService = new LevelService();
    }

    @Test
    void calculateLevel_zeroXp_returnsLevel1() {
        assertEquals(1, levelService.calculateLevel(0));
    }

    @Test
    void calculateLevel_justBelowLevel2_returnsLevel1() {
        // Level 2 requires 100 XP (BASE_XP)
        assertEquals(1, levelService.calculateLevel(99));
    }

    @Test
    void calculateLevel_exactlyLevel2_returnsLevel2() {
        assertEquals(2, levelService.calculateLevel(100));
    }

    @Test
    void calculateLevel_justBelowLevel3_returnsLevel2() {
        // Level 3 requires 100 + 150 = 250 total XP
        assertEquals(2, levelService.calculateLevel(249));
    }

    @Test
    void calculateLevel_exactlyLevel3_returnsLevel3() {
        assertEquals(3, levelService.calculateLevel(250));
    }

    @Test
    void calculateLevel_level4_threshold() {
        // Level 4 requires 100 + 150 + 200 = 450 total XP
        assertEquals(3, levelService.calculateLevel(449));
        assertEquals(4, levelService.calculateLevel(450));
    }

    @Test
    void calculateLevel_level5_threshold() {
        // Level 5 requires 100 + 150 + 200 + 250 = 700 total XP
        assertEquals(4, levelService.calculateLevel(699));
        assertEquals(5, levelService.calculateLevel(700));
    }

    @Test
    void calculateLevel_highXp_returnsHighLevel() {
        // Very high XP should return a high level without errors
        int level = levelService.calculateLevel(100_000);
        assertTrue(level > 10);
    }

    @Test
    void calculateLevel_negativeXp_returnsLevel1() {
        assertEquals(1, levelService.calculateLevel(-1));
    }

    @Test
    void calculateLevel_progressiveDifficulty() {
        // Each level should require more XP than the previous
        int prevXp = 0;
        for (int targetLevel = 2; targetLevel <= 6; targetLevel++) {
            int xp = 0;
            while (levelService.calculateLevel(xp) < targetLevel) {
                xp++;
            }
            int xpNeeded = xp - prevXp;
            assertTrue(xpNeeded > 0, "Level " + targetLevel + " should require positive XP");
            prevXp = xp;
        }
    }
}
