package se.sobriety.nextstep.entity;

/**
 * Kategorier för challenges/utmaningar
 */
public enum ChallengeCategory {
    MENTAL_HEALTH("Mental hälsa", "mental"),
    PHYSICAL_ACTIVITY("Fysisk aktivitet", "physical"),
    FOCUS_DISCIPLINE("Fokus & disciplin", "focus"),
    PERSONAL_DEVELOPMENT("Personlig utveckling", "growth"),
    DRAWING_EXERCISES("Ritövningar", "drawing"),
    HEALTHY_HABITS("Hälsosamma vanor", "habits"),
    SOCIAL_SKILLS("Sociala färdigheter", "social"),
    EMOTIONAL_AWARENESS("Emotionell medvetenhet", "emotional");

    private final String displayName;
    private final String id;

    ChallengeCategory(String displayName, String id) {
        this.displayName = displayName;
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getId() {
        return id;
    }
}
