package se.sobriety.nextstep.entity;

/**
 * Svårighetsgrad för challenges
 */
public enum ChallengeDifficulty {
    EASY("Lätt", 10),
    MEDIUM("Mellan", 25),
    HARD("Svår", 50);

    private final String displayName;
    private final int points;

    ChallengeDifficulty(String displayName, int points) {
        this.displayName = displayName;
        this.points = points;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPoints() {
        return points;
    }
}

