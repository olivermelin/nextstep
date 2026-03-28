package se.sobriety.nextstep.service.ai;

import org.springframework.stereotype.Component;
import se.sobriety.nextstep.entity.*;
import se.sobriety.nextstep.repository.ChallengeRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Bygger en dynamisk svensk systemprompt per användare för Claude API.
 * Anpassar ton baserat på CoachPersonality och inkluderar återhämtningskontext.
 */
@Component
public class SystemPromptBuilder {

    private final ChallengeRepository challengeRepository;

    public SystemPromptBuilder(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    /**
     * Bygger komplett systemprompt för Claude baserat på användarens återhämtningskontext.
     */
    public String build(UserSettings settings, UserProgress progress, int completedChallenges,
                        CoachPersonality personality, CrisisLevel currentCrisisLevel) {

        StringBuilder prompt = new StringBuilder();

        // Grundidentitet
        prompt.append("Du är en återhämtningscoach i NextStep-appen – en svensk app som hjälper människor i återhämtning från beroende. ");
        prompt.append("Du är en stödjande, icke-dömande återhämtningskompis som pratar med användaren på svenska.\n\n");

        // Personlighetsanpassning
        prompt.append(buildPersonalityInstructions(personality));

        // Grundregler
        prompt.append("\nGRUNDREGLER:\n");
        prompt.append("- Svara ALLTID på svenska\n");
        prompt.append("- Var icke-dömande och respektfull\n");
        prompt.append("- Fokusera på små, konkreta steg framåt\n");
        prompt.append("- Uppmuntra självreflektion\n");
        prompt.append("- Håll svaren koncisa (2-4 meningar) om inte användaren ber om mer\n");
        prompt.append("- Du är INTE läkare eller terapeut – hänvisa till professionell hjälp vid behov\n");
        prompt.append("- Fira framsteg och styrkor hos användaren\n");
        prompt.append("\nNAMNREGEL – följ dessa strikt:\n");
        prompt.append("- Använd ALDRIG användarens namn i svaren. ALDRIG. Det låter robotaktigt.\n");
        prompt.append("- ENDA undantaget: allra första hälsningen i en ny konversation. Då skriver du 'Hej [förnamn]!' – ENBART förnamnet.\n");
        prompt.append("- Förnamnet = BARA det första ordet. Om namnet är 'Oliver Melin' skriver du 'Hej Oliver!' – ALDRIG 'Oliver Melin'.\n");
        prompt.append("- I alla efterföljande svar: använd INTE namnet alls. Svara direkt utan att tilltala personen vid namn.\n\n");

        // Formateringsregler
        prompt.append("FORMATERING:\n");
        prompt.append("- Skriv i ren text utan markdown-formatering\n");
        prompt.append("- Använd INTE **fetstil**, *kursiv*, #rubriker eller andra markdown-symboler\n");
        prompt.append("- Använd ABSOLUT INGA emojis eller unicode-symboler (inga 💙🌟⭐🚶🧘 etc.) – varken ensamma eller i parenteser\n");
        prompt.append("- Skriv aldrig emojis inuti parenteser som (🚶) eller (💪) – skriv helt utan parenteser om det inte är nödvändigt\n");
        prompt.append("- Skriv naturligt och varmt utan speciella tecken\n");
        prompt.append("- Separera stycken med en tom rad (dubbelt radbryte) om svaret har flera delar\n\n");

        // Användarkontext
        prompt.append("ANVÄNDARENS PROFIL:\n");

        if (settings != null) {
            if (settings.getName() != null && !settings.getName().equals("Default Name")) {
                prompt.append("- Namn: ").append(settings.getName()).append("\n");
            }

            if (settings.getRecoveryStage() != null) {
                prompt.append("- Återhämtningsstadium: ").append(translateRecoveryStage(settings.getRecoveryStage())).append("\n");
                prompt.append("- ").append(getDaysSoberDescription(settings.getRecoveryStage())).append("\n");
            }

            if (settings.getBackgroundInfo() != null) {
                BackgroundInfo bg = settings.getBackgroundInfo();

                if (bg.getSubstanceHistory() != null && !bg.getSubstanceHistory().isEmpty()) {
                    prompt.append("- Beroendetyp: ").append(
                            bg.getSubstanceHistory().stream()
                                    .map(this::translateSubstanceType)
                                    .collect(Collectors.joining(", "))
                    ).append("\n");
                }

                if (bg.getCurrentSituation() != null) {
                    prompt.append("- Nuvarande situation: ").append(translateSituation(bg.getCurrentSituation())).append("\n");
                }

                if (bg.getAge() != null) {
                    prompt.append("- Ålder: ").append(bg.getAge()).append("\n");
                }
            }

            if (settings.getUserGoals() != null && !settings.getUserGoals().isEmpty()) {
                prompt.append("- Mål: ").append(
                        settings.getUserGoals().stream()
                                .map(this::translateGoal)
                                .collect(Collectors.joining(", "))
                ).append("\n");
            }
        }

        // Framsteg
        if (progress != null) {
            prompt.append("\nFRAMSTEG:\n");
            prompt.append("- Nivå: ").append(progress.getLevel()).append("\n");
            prompt.append("- Totala poäng: ").append(progress.getTotalPoints()).append("\n");
            prompt.append("- Genomförda utmaningar: ").append(completedChallenges).append("\n");
        }

        // Krishantering baserat på risknivå
        if (currentCrisisLevel == CrisisLevel.ELEVATED) {
            prompt.append("\n⚠️ VIKTIGT – FÖRHÖJD RISK:\n");
            prompt.append("Användaren visar tecken på att ha det svårt. ");
            prompt.append("Var extra empatisk och stödjande. ");
            prompt.append("Påminn försiktigt om att hjälp finns tillgänglig: Mind Självmordslinjen 90101.\n");
        }

        // Vid HIGH-nivå (ELEVATED): lyft fram krishjälp
        if (currentCrisisLevel != CrisisLevel.NONE) {
            prompt.append("\nKRISRESURSER att nämna vid behov:\n");
            prompt.append("- Mind Självmordslinjen: 90101 (dygnet runt)\n");
            prompt.append("- Jourhavande medmänniska: 08-702 16 80\n");
            prompt.append("- Vid akut fara: ring 112\n");
        }

        // Tillgängliga challenges för rekommendationer
        prompt.append(buildChallengeCatalog());

        return prompt.toString();
    }

    /**
     * Bygger en kompakt katalog av alla tillgängliga challenges för AI:n att referera till.
     */
    private String buildChallengeCatalog() {
        List<Challenge> challenges = challengeRepository.findAll();
        if (challenges.isEmpty()) {
            return "";
        }

        StringBuilder catalog = new StringBuilder();
        catalog.append("\nTILLGÄNGLIGA UTMANINGAR I APPEN:\n");
        catalog.append("När användaren pratar om aktiviteter, träning, mindfulness eller annat som matchar en utmaning, ");
        catalog.append("rekommendera relevanta utmaningar med exakt formatet [CHALLENGE:id] (utan mellanslag) i ditt svar.\n");
        catalog.append("VIKTIGT: Skriv taggen exakt så här: [CHALLENGE:5] – inte [CHALLENGE: 5] eller [Challenge:5].\n");
        catalog.append("Rekommendera 1-2 utmaningar när det passar naturligt. Tvinga inte in rekommendationer.\n");
        catalog.append("Taggen är osynlig för användaren – den ersätts med en klickbar knapp i appen.\n\n");

        for (Challenge c : challenges) {
            catalog.append("ID=").append(c.getId()).append(": ")
                    .append(c.getTitle())
                    .append(" (").append(c.getCategory().getDisplayName())
                    .append(", ").append(translateDifficulty(c.getDifficulty()))
                    .append(", ").append(c.getDurationMinutes()).append(" min)")
                    .append("\n");
        }

        return catalog.toString();
    }

    private String translateDifficulty(ChallengeDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> "Lätt";
            case MEDIUM -> "Medel";
            case HARD -> "Svår";
        };
    }

    private String buildPersonalityInstructions(CoachPersonality personality) {
        if (personality == null) {
            personality = CoachPersonality.SUPPORTIVE;
        }

        return switch (personality) {
            case SUPPORTIVE -> """
                    DIN TON: Varm, omtänksam och stödjande. Du lyssnar aktivt och validerar känslor. \
                    Använd mjuka formuleringar och visa att du bryr dig.
                    
                    """;
            case MOTIVATIONAL -> """
                    DIN TON: Energisk, peppande och framåtblickande. Du fokuserar på styrkor och möjligheter. \
                    Använd uppmuntrande språk och hjälp användaren se sin potential.
                    
                    """;
            case REFLECTIVE -> """
                    DIN TON: Lugn, eftertänksam och filosofisk. Du ställer öppna frågor som bjuder in till reflektion. \
                    Hjälp användaren utforska sina tankar och känslor i egen takt.
                    
                    """;
            case DIRECT -> """
                    DIN TON: Ärlig, rakt på sak och konkret. Du ger tydliga råd och förslag. \
                    Var respektfull men undvik att linda in budskap i onödiga ord.
                    
                    """;
        };
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

    private String getDaysSoberDescription(RecoveryStage stage) {
        return switch (stage) {
            case ACTIVE_USE -> "Personen använder fortfarande – fokusera på skadereducering och hopp";
            case TRYING_TO_QUIT -> "Personen försöker sluta – stöd motivationen och hantera sug";
            case ONE_TO_SEVEN_DAYS -> "Tidig återhämtning – fokusera på att ta en dag i taget";
            case ONE_TO_FOUR_WEEKS -> "Första veckorna – bygga nya rutiner, hantera abstinens";
            case ONE_TO_SIX_MONTHS -> "Stabil fas – stärka nya vanor, identifiera triggers";
            case SIX_PLUS_MONTHS -> "Halvåret passerat – fira framgång, fortsätt förebygga återfall";
            case LONG_TERM_RECOVERY -> "Långvarig nykterhet – underhåll, mening och hjälpa andra";
        };
    }

    private String translateSubstanceType(SubstanceType type) {
        return switch (type) {
            case ALCOHOL -> "Alkohol";
            case CANNABIS -> "Cannabis";
            case STIMULANTS -> "Centralstimulantia";
            case OPIOIDS -> "Opioider";
            case PRESCRIPTION_MEDS -> "Receptbelagda läkemedel";
            case GAMBLING -> "Spelberoende";
            case OTHER -> "Annat";
            case PREFER_NOT_TO_SAY -> "Vill inte ange";
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

