package se.sobriety.nextstep.entity;

/**
 * Kategorier för challenges/utmaningar
 */
public enum ChallengeCategory {
    MENTAL_HEALTH("Mental hälsa"),
    PHYSICAL_ACTIVITY("Fysisk aktivitet"),
    FOCUS_DISCIPLINE("Fokus & disciplin"),
    PERSONAL_DEVELOPMENT("Personlig utveckling"),
    DRAWING_EXERCISES("Ritövningar"),
    HEALTHY_HABITS("Hälsosamma vanor"),
    SOCIAL_SKILLS("Sociala färdigheter"),
    EMOTIONAL_AWARENESS("Emotionell medvetenhet");

    private final String displayName;

    ChallengeCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

