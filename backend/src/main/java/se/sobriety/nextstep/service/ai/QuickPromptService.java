package se.sobriety.nextstep.service.ai;

import org.springframework.stereotype.Service;
import se.sobriety.nextstep.dto.QuickPromptDto;
import se.sobriety.nextstep.dto.StreakResponseDto;
import se.sobriety.nextstep.entity.DailyCheckIn;
import se.sobriety.nextstep.entity.RecoveryStage;
import se.sobriety.nextstep.entity.UserSettings;
import se.sobriety.nextstep.service.DailyCheckInService;
import se.sobriety.nextstep.service.StreakService;
import se.sobriety.nextstep.service.UserSettingsService;

import java.util.ArrayList;
import java.util.List;

/**
 * Genererar personaliserade snabbfrågor baserat på användarens kontext.
 */
@Service
public class QuickPromptService {

    private final UserSettingsService userSettingsService;
    private final DailyCheckInService dailyCheckInService;
    private final StreakService streakService;

    public QuickPromptService(UserSettingsService userSettingsService,
                              DailyCheckInService dailyCheckInService,
                              StreakService streakService) {
        this.userSettingsService = userSettingsService;
        this.dailyCheckInService = dailyCheckInService;
        this.streakService = streakService;
    }

    /**
     * Returnerar 4 kontextanpassade snabbfrågor för AI-coachens sidopanel.
     */
    public List<QuickPromptDto> getQuickPrompts(String userId) {
        List<QuickPromptDto> prompts = new ArrayList<>();

        UserSettings settings = null;
        DailyCheckIn todaysCheckIn = null;
        StreakResponseDto streakData = null;

        try { settings = userSettingsService.getOrCreateSettings(userId); } catch (Exception ignored) {}
        try { todaysCheckIn = dailyCheckInService.getTodaysCheckInEntity(userId).orElse(null); } catch (Exception ignored) {}
        try { streakData = streakService.getStreak(userId); } catch (Exception ignored) {}

        RecoveryStage stage = settings != null ? settings.getRecoveryStage() : null;

        // 1. Humörbaserad prompt
        if (todaysCheckIn != null) {
            int mood = todaysCheckIn.getMoodScore();
            if (mood <= 2) {
                prompts.add(new QuickPromptDto("Heart", "Jag har det tufft idag",
                        "Jag checkade in med humör " + mood + "/5 idag och har det ganska jobbigt. Kan du hjälpa mig?"));
            } else if (mood >= 4) {
                prompts.add(new QuickPromptDto("Zap", "Utnyttja min energi",
                        "Jag mår bra idag (humör " + mood + "/5). Vad kan jag göra för att utnyttja den här positiva energin?"));
            } else {
                prompts.add(new QuickPromptDto("Heart", "Hur mår jag egentligen?",
                        "Hjälp mig reflektera över hur jag mår just nu och vad jag behöver idag."));
            }
        } else {
            prompts.add(new QuickPromptDto("Heart", "Hur mår jag?",
                    "Hjälp mig reflektera över hur jag mår just nu och vad jag behöver idag."));
        }

        // 2. Streak/motivations-prompt
        if (streakData != null && streakData.streakActive() && streakData.currentStreak() >= 3) {
            prompts.add(new QuickPromptDto("Zap", streakData.currentStreak() + " dagar i rad!",
                    "Jag har en streak på " + streakData.currentStreak() + " dagar. Peppa mig att fortsätta!"));
        } else {
            prompts.add(new QuickPromptDto("Zap", "Hitta motivation",
                    "Jag behöver lite motivation just nu. Kan du hjälpa mig hitta styrka att fortsätta?"));
        }

        // 3. Stage-baserad prompt
        if (stage != null) {
            prompts.add(buildStagePrompt(stage));
        } else {
            prompts.add(new QuickPromptDto("Target", "Sätt ett mål",
                    "Hjälp mig sätta ett konkret mål som jag kan jobba mot den närmaste veckan."));
        }

        // 4. Alltid: tips/råd
        prompts.add(new QuickPromptDto("Lightbulb", "Tips för idag",
                "Ge mig ett praktiskt tips som jag kan använda just idag för att må lite bättre."));

        return prompts;
    }

    private QuickPromptDto buildStagePrompt(RecoveryStage stage) {
        return switch (stage) {
            case ACTIVE_USE -> new QuickPromptDto("Target", "Vill göra en förändring",
                    "Jag funderar på att göra en förändring. Kan vi prata om vad ett första steg kan vara?");
            case TRYING_TO_QUIT -> new QuickPromptDto("Target", "Hantera sug",
                    "Jag känner sug just nu. Vad kan jag göra istället?");
            case ONE_TO_SEVEN_DAYS -> new QuickPromptDto("Target", "Ta mig genom dagen",
                    "Jag är i tidiga dagar av nykterhet. Hjälp mig komma igenom den här dagen.");
            case ONE_TO_FOUR_WEEKS -> new QuickPromptDto("Target", "Nya rutiner",
                    "Hjälp mig bygga nya vardagsrutiner som stödjer min återhämtning.");
            case ONE_TO_SIX_MONTHS -> new QuickPromptDto("Target", "Identifiera triggers",
                    "Hjälp mig identifiera mina triggers och hur jag kan hantera dem.");
            case SIX_PLUS_MONTHS -> new QuickPromptDto("Target", "Nästa steg",
                    "Jag har kommit långt i min återhämtning. Vad kan jag fokusera på härnäst?");
            case LONG_TERM_RECOVERY -> new QuickPromptDto("Target", "Ge tillbaka",
                    "Jag har långvarig nykterhet. Hur kan jag hjälpa andra i deras resa?");
        };
    }
}
