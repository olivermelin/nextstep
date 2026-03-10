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
 */
@Service
public class CrisisDetectionService {

    private static final Logger log = LoggerFactory.getLogger(CrisisDetectionService.class);

    // CRITICAL – direkta krisuttryck kring självmord, överdos, självskada
    private static final List<Pattern> CRITICAL_PATTERNS = List.of(
            Pattern.compile("(?i)\\b(vill|tänker|planerar|ska|kommer)\\s+(ta\\s+livet|döda\\s+mig|begå\\s+självmord|avsluta\\s+allt)"),
            Pattern.compile("(?i)\\b(självmord|suicid)\\b"),
            Pattern.compile("(?i)\\b(överdos|overdose)\\b"),
            Pattern.compile("(?i)\\b(vill\\s+inte\\s+leva|orkar\\s+inte\\s+leva|vill\\s+dö|bättre\\s+utan\\s+mig)"),
            Pattern.compile("(?i)\\b(tagit\\s+för\\s+mycket|tagit\\s+en\\s+överdos)"),
            Pattern.compile("(?i)\\b(skära\\s+mig|skadar\\s+mig\\s+själv|självskada)"),
            Pattern.compile("(?i)\\b(ingen\\s+mening\\s+att\\s+leva)"),
            Pattern.compile("(?i)\\b(suicide|kill\\s+myself|end\\s+my\\s+life|want\\s+to\\s+die)")
    );

    // ELEVATED – återfall kombinerat med hopplöshetsmönster
    private static final List<Pattern> ELEVATED_PATTERNS = List.of(
            Pattern.compile("(?i)\\b(återfall|relaps|börjat\\s+dricka\\s+igen|tagit\\s+droger\\s+igen|börjat\\s+använda\\s+igen)"),
            Pattern.compile("(?i)\\b(hopplös|ingen\\s+mening|ger\\s+upp|kan\\s+inte\\s+mer|klarar\\s+inte)"),
            Pattern.compile("(?i)\\b(ensam|ingen\\s+bryr\\s+sig|ingen\\s+förstår)"),
            Pattern.compile("(?i)\\b(panik|ångest\\s+attack|panikångest)"),
            Pattern.compile("(?i)\\b(sug\\s+efter|craving|måste\\s+ha|abstinens)")
    );

    // Hopplöshetsförstärkare som höjer ELEVATED till CRITICAL om kombinerade med återfall
    private static final List<Pattern> HOPELESSNESS_PATTERNS = List.of(
            Pattern.compile("(?i)\\b(hopplöst|meningslöst|ingen\\s+idé|aldrig\\s+bli\\s+bättre|ger\\s+upp\\s+helt)"),
            Pattern.compile("(?i)\\b(ingen\\s+kan\\s+hjälpa|inget\\s+fungerar|allt\\s+är\\s+kört)")
    );

    private static final List<Pattern> RELAPSE_PATTERNS = List.of(
            Pattern.compile("(?i)\\b(återfall|relaps|druckit|tagit\\s+droger|använt\\s+igen|börjat\\s+igen)")
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

