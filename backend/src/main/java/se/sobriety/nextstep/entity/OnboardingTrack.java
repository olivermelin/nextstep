package se.sobriety.nextstep.entity;

/**
 * Onboarding-spår väljs av användaren i steg 1.
 *
 * CONSUMER  — Konsumentspår: vanor, välmående och personlig utveckling.
 *             Artikel-9-data (substanshistorik, klinisk kontext) samlas INTE in.
 *
 * RECOVERY  — Återhämtningsspår: stöd vid substansberoende och återhämtning.
 *             Fullständig onboarding med substanshistorik och återhämtningsstadium.
 */
public enum OnboardingTrack {
    CONSUMER,
    RECOVERY
}
