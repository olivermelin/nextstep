package se.sobriety.nextstep.service.ai;

import org.springframework.stereotype.Component;
import se.sobriety.nextstep.entity.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Bygger kontextuell information för AI-coachen
 */
@Component
public class AIContextBuilder {

    /**
     * Bygger en system prompt för AI-coachen baserat på användarens profil
     */
    public String buildSystemPrompt(UserSettings settings, UserProgress progress, RecoveryStage recoveryStage) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Du är en empatisk och professionell återhämtningscoach för NextStep-appen. ");
        prompt.append("Din roll är att stödja användare i deras återhämtning från beroende. ");
        prompt.append("Du ska vara uppmuntrande, icke-dömande och fokusera på små steg framåt.\n\n");

        prompt.append("RIKTLINJER:\n");
        prompt.append("- Var alltid empatisk och stödjande\n");
        prompt.append("- Fokusera på små, konkreta steg\n");
        prompt.append("- Uppmuntra självreflektion\n");
        prompt.append("- Var icke-dömande och respektfull\n");
        prompt.append("- Använd svenska språket\n");
        prompt.append("- Håll svaren koncisa (max 2-3 meningar om inte användaren frågar mer)\n");
        prompt.append("- Referera till deras framsteg när relevant\n\n");

        if (settings != null && settings.getName() != null) {
            prompt.append("Användarens namn: ").append(settings.getName()).append("\n");
        }

        if (progress != null) {
            prompt.append("Nuvarande nivå: ").append(progress.getLevel()).append("\n");
            prompt.append("Totala poäng: ").append(progress.getTotalPoints()).append("\n");
        }

        if (recoveryStage != null) {
            prompt.append("Återhämtningsstadium: ").append(translateRecoveryStage(recoveryStage)).append("\n");
        }

        return prompt.toString();
    }

    /**
     * Bygger användarkontext för att ge AI mer information
     */
    public String buildUserContext(UserSettings settings, List<UserGoal> goals,
                                    BackgroundInfo backgroundInfo, int completedChallenges) {
        StringBuilder context = new StringBuilder();

        context.append("ANVÄNDARKONTEXT:\n");

        if (goals != null && !goals.isEmpty()) {
            context.append("Mål: ");
            context.append(goals.stream()
                    .map(this::translateGoal)
                    .collect(Collectors.joining(", ")));
            context.append("\n");
        }

        if (backgroundInfo != null) {
            if (backgroundInfo.getAge() != null) {
                context.append("Ålder: ").append(backgroundInfo.getAge()).append("\n");
            }

            if (backgroundInfo.getCurrentSituation() != null) {
                context.append("Nuvarande situation: ")
                        .append(translateSituation(backgroundInfo.getCurrentSituation()))
                        .append("\n");
            }
        }

        context.append("Genomförda utmaningar: ").append(completedChallenges).append("\n");

        return context.toString();
    }

    /**
     * Bygger en fallback prompt när AI inte är tillgänglig
     */
    public String buildFallbackResponse(String userName) {
        String[] motivationalMessages = {
                "Kom ihåg att varje liten framsteg räknas. Du tar ett steg i taget!",
                "Bra jobbat att du är här idag. Det visar styrka och engagemang.",
                "Fokusera på nuet. Du klarar av det här, ett ögonblick i taget.",
                "Återhämtning är en resa, inte ett mål. Var stolt över hur långt du kommit.",
                "Du är starkare än du tror. Fortsätt framåt!",
                "Varje dag nykter är en seger. Fira de små framstegen."
        };

        int index = (int) (System.currentTimeMillis() % motivationalMessages.length);
        String message = motivationalMessages[index];

        if (userName != null && !userName.isBlank() && !userName.equals("Default Name")) {
            return userName + ", " + message.substring(0, 1).toLowerCase() + message.substring(1);
        }

        return message;
    }

    private String translateRecoveryStage(RecoveryStage stage) {
        return switch (stage) {
            case ACTIVE_USE -> "Aktivt bruk";
            case TRYING_TO_QUIT -> "Försöker sluta";
            case ONE_TO_SEVEN_DAYS -> "1-7 dagar nykter";
            case ONE_TO_FOUR_WEEKS -> "1-4 veckor nykter";
            case ONE_TO_SIX_MONTHS -> "1-6 månader nykter";
            case SIX_PLUS_MONTHS -> "6+ månader nykter";
            case LONG_TERM_RECOVERY -> "Långvarig återhämtning";
        };
    }

    private String translateGoal(UserGoal goal) {
        return switch (goal) {
            case DAILY_STABILITY -> "Daglig stabilitet";
            case BETTER_ROUTINES -> "Bättre rutiner";
            case REDUCE_SUBSTANCES -> "Minska substansanvändning";
            case MENTAL_HEALTH -> "Mental hälsa";
            case STRUCTURE_MOTIVATION -> "Struktur och motivation";
            case STAY_CLEAN -> "Hålla sig ren";
            case OTHER -> "Annat";
        };
    }

    private String translateSituation(CurrentSituation situation) {
        return switch (situation) {
            case IN_TREATMENT -> "I behandling";
            case RECENTLY_COMPLETED -> "Nyligen avslutat behandling";
            case STRUGGLING_NOT_IN_TREATMENT -> "Kämpar, inte i behandling";
            case SUPPORTING_SOMEONE -> "Stödjer någon";
            case PREFER_NOT_TO_SAY -> "Vill inte säga";
        };
    }
}

