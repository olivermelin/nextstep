package se.sobriety.nextstep.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.sobriety.nextstep.entity.CrisisLevel;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Skannar användarmeddelanden efter kris-signaler.
 * Returnerar CrisisLevel och vid CRITICAL ett fördefinierat säkert svar.
 *
 * OBS: Använder (?iu) för Unicode-medveten case-insensitivity (krävs för å, ä, ö).
 * Använder lookahead/lookbehind istället för \b som ordgränser,
 * eftersom Java \b inte hanterar svenska tecken som ordkaraktärer.
 */
@Service
public class CrisisDetectionService {

    private static final Logger log = LoggerFactory.getLogger(CrisisDetectionService.class);

    // Flaggor: case-insensitive + unicode case-folding (å/Å, ä/Ä, ö/Ö)
    private static final String FLAGS = "(?iu)";

    // Unicode-medveten ordgräns: matchar vid start av sträng eller efter whitespace
    private static final String WB_START = "(?<=\\s|^)";
    // Unicode-medveten ordgräns: matchar vid slut av sträng eller före whitespace/skiljetecken
    private static final String WB_END = "(?=\\s|[.,;:!?]|$)";

    // CRITICAL – direkta krisuttryck kring självmord, överdos, självskada
    private static final List<Pattern> CRITICAL_PATTERNS = List.of(
            // "vill/tänker/planerar (på att / att) ta livet av mig / döda mig / begå självmord"
            Pattern.compile(FLAGS + WB_START + "(vill|tänker|planerar|ska|kommer)(?:\\s+(?:på\\s+)?att)?\\s+(ta\\s+livet|döda\\s+mig|begå\\s+självmord|avsluta\\s+allt)"),
            Pattern.compile(FLAGS + WB_START + "(självmord|suicid)" + WB_END),
            Pattern.compile(FLAGS + WB_START + "(överdos|overdose)" + WB_END),
            // "vill dö" — men INTE i positiva sammanhang som "vill dö av lycka"
            Pattern.compile(FLAGS + WB_START + "(vill\\s+inte\\s+leva|orkar\\s+inte\\s+leva|bättre\\s+utan\\s+mig)"),
            Pattern.compile(FLAGS + "vill\\s+dö(?!\\s+av\\s+(?:lycka|glädje|skratt|framgång|kärlek))"),
            Pattern.compile(FLAGS + WB_START + "(tagit\\s+för\\s+mycket|tagit\\s+en\\s+överdos)"),
            Pattern.compile(FLAGS + WB_START + "(skära\\s+mig|skadar\\s+mig\\s+själv|självskada)" + WB_END),
            Pattern.compile(FLAGS + WB_START + "(ingen\\s+mening\\s+att\\s+leva)"),
            Pattern.compile(FLAGS + WB_START + "(suicide|kill\\s+myself|end\\s+my\\s+life|want\\s+to\\s+die)")
    );

    // ELEVATED – återfall kombinerat med hopplöshetsmönster
    private static final List<Pattern> ELEVATED_PATTERNS = List.of(
            Pattern.compile(FLAGS + WB_START + "(återfall|relaps|börjat\\s+dricka\\s+igen|druckit\\s+igen|druckit|tagit\\s+droger\\s+igen|börjat\\s+använda\\s+igen|använda\\s+igen)"),
            Pattern.compile(FLAGS + WB_START + "(hopplös|ingen\\s+mening|ger\\s+upp|kan\\s+inte\\s+mer|klarar\\s+inte)"),
            Pattern.compile(FLAGS + WB_START + "(meningslöst|(?:ingenting|inget)\\s+(?:känns|är|verkar)\\s+meningsfullt)"),
            Pattern.compile(FLAGS + WB_START + "(ensam|ingen\\s+bryr\\s+sig|ingen\\s+förstår)"),
            Pattern.compile(FLAGS + WB_START + "(panik|ångest\\s+attack|panikångest)" + WB_END),
            Pattern.compile(FLAGS + WB_START + "(sug\\s+efter|craving|måste\\s+ha|abstinens)" + WB_END)
    );

    // Hopplöshetsförstärkare som höjer ELEVATED till CRITICAL om kombinerade med återfall
    private static final List<Pattern> HOPELESSNESS_PATTERNS = List.of(
            Pattern.compile(FLAGS + WB_START + "(hopplöst|meningslöst|ingen\\s+idé|aldrig\\s+bli\\s+bättre|ger\\s+upp\\s+helt)"),
            Pattern.compile(FLAGS + WB_START + "(ingen\\s+kan\\s+hjälpa|inget\\s+fungerar|allt\\s+är\\s+kört)")
    );

    private static final List<Pattern> RELAPSE_PATTERNS = List.of(
            Pattern.compile(FLAGS + WB_START + "(återfall|relaps|druckit|tagit\\s+droger|använt\\s+igen|använda\\s+igen|börjat\\s+igen|börjat\\s+använda\\s+igen)")
    );

    /**
     * Analyserar ett meddelande och returnerar krisnivån.
     */
    public CrisisLevel analyze(String message) {
        if (message == null || message.isBlank()) {
            return CrisisLevel.NONE;
        }

        String normalizedMessage = message.trim();

        // Kolla CRITICAL först
        for (Pattern pattern : CRITICAL_PATTERNS) {
            if (pattern.matcher(normalizedMessage).find()) {
                log.warn("CRITICAL crisis signal detected in message");
                return CrisisLevel.CRITICAL;
            }
        }

        // Kolla kombinationen återfall + hopplöshet → CRITICAL
        boolean hasRelapse = RELAPSE_PATTERNS.stream()
                .anyMatch(p -> p.matcher(normalizedMessage).find());
        boolean hasHopelessness = HOPELESSNESS_PATTERNS.stream()
                .anyMatch(p -> p.matcher(normalizedMessage).find());

        if (hasRelapse && hasHopelessness) {
            log.warn("CRITICAL crisis signal: relapse + hopelessness pattern detected");
            return CrisisLevel.CRITICAL;
        }

        // Kolla ELEVATED
        int elevatedMatches = 0;
        for (Pattern pattern : ELEVATED_PATTERNS) {
            if (pattern.matcher(normalizedMessage).find()) {
                elevatedMatches++;
            }
        }

        if (elevatedMatches >= 1) {
            log.info("ELEVATED crisis signal detected ({} matches)", elevatedMatches);
            return CrisisLevel.ELEVATED;
        }

        return CrisisLevel.NONE;
    }

    /**
     * Returnerar ett fördefinierat krishanteringssvar på svenska.
     * Används vid CRITICAL-nivå istället för att anropa Claude API.
     */
    public String getCrisisResponse() {
        return """
                Jag hör att du har det väldigt svårt just nu, och jag vill att du vet att du inte är ensam. \
                Det du känner är verkligt och viktigt.

                **Ring eller kontakta hjälp nu:**
                📞 **Mind Självmordslinjen:** 90101 (dygnet runt)
                📞 **Jourhavande medmänniska:** 08-702 16 80 (kväll/natt)
                📞 **112** – vid akut fara
                💬 **mind.se** – chatt och stöd

                Du förtjänar hjälp och det finns människor som vill lyssna. \
                Var snäll och kontakta någon av resurserna ovan – de är utbildade att hjälpa i precis den här situationen.\
                """;
    }
}
