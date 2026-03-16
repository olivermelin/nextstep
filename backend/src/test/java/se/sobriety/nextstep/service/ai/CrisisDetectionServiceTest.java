package se.sobriety.nextstep.service.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import se.sobriety.nextstep.entity.CrisisLevel;

import static org.junit.jupiter.api.Assertions.*;

class CrisisDetectionServiceTest {

    private CrisisDetectionService service;

    @BeforeEach
    void setUp() {
        service = new CrisisDetectionService();
    }

    // ===================== NONE =====================

    @ParameterizedTest
    @NullAndEmptySource
    void analyze_nullOrEmpty_returnsNone(String message) {
        assertEquals(CrisisLevel.NONE, service.analyze(message));
    }

    @Test
    void analyze_blankString_returnsNone() {
        assertEquals(CrisisLevel.NONE, service.analyze("   "));
    }

    @Test
    void analyze_normalMessage_returnsNone() {
        assertEquals(CrisisLevel.NONE, service.analyze("Jag vill ta en promenad idag"));
    }

    @Test
    void analyze_positiveRecoveryMessage_returnsNone() {
        assertEquals(CrisisLevel.NONE, service.analyze("Det har varit en bra vecka"));
    }

    // ===================== CRITICAL: Swedish suicide expressions =====================

    @ParameterizedTest
    @ValueSource(strings = {
            "Jag vill ta livet av mig",
            "Jag tänker begå självmord",
            "Jag planerar att döda mig",
            "Jag vill inte leva längre",
            "Alla har det bättre utan mig",
            "Jag orkar inte leva mer"
    })
    void analyze_swedishSuicideExpressions_returnsCritical(String message) {
        assertEquals(CrisisLevel.CRITICAL, service.analyze(message),
                "Should detect CRITICAL for: " + message);
    }

    // ===================== CRITICAL: Keywords =====================

    @Test
    void analyze_suicideKeyword_returnsCritical() {
        assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag funderar kring självmord just nu"));
    }

    @Test
    void analyze_suicideKeyword_standalone_returnsCritical() {
        assertEquals(CrisisLevel.CRITICAL, service.analyze("självmord"));
    }

    @Test
    void analyze_suicidKeyword_returnsCritical() {
        assertEquals(CrisisLevel.CRITICAL, service.analyze("suicid"));
    }

    @Test
    void analyze_overdoseSwedish_returnsCritical() {
        assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag tog en överdos igår"));
    }

    @Test
    void analyze_overdoseSwedish_standalone_returnsCritical() {
        assertEquals(CrisisLevel.CRITICAL, service.analyze("överdos"));
    }

    @Test
    void analyze_overdoseEnglish_returnsCritical() {
        assertEquals(CrisisLevel.CRITICAL, service.analyze("I took an overdose"));
    }

    @Test
    void analyze_tookOverdose_returnsCritical() {
        assertEquals(CrisisLevel.CRITICAL, service.analyze("Har tagit en överdos idag"));
    }

    @Test
    void analyze_selfHarm_returnsCritical() {
        assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag vill skära mig"));
    }

    @Test
    void analyze_selfHarmPattern_returnsCritical() {
        assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag skadar mig själv"));
    }

    @Test
    void analyze_selfHarmKeyword_returnsCritical() {
        assertEquals(CrisisLevel.CRITICAL, service.analyze("Har problem med självskada"));
    }

    @Test
    void analyze_noMeaningToLive_returnsCritical() {
        assertEquals(CrisisLevel.CRITICAL, service.analyze("Det finns ingen mening att leva"));
    }

    @Test
    void analyze_wantToDie_returnsCritical() {
        assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag vill dö"));
    }

    @Test
    void analyze_tookTooMuch_returnsCritical() {
        assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag har tagit för mycket"));
    }

    @Test
    void analyze_planSuicide_returnsCritical() {
        assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag planerar ta livet av mig"));
    }

    // ===================== CRITICAL: English patterns =====================

    @ParameterizedTest
    @ValueSource(strings = {
            "I want to kill myself",
            "I want to end my life",
            "I want to die",
            "suicide"
    })
    void analyze_englishCrisisExpressions_returnsCritical(String message) {
        assertEquals(CrisisLevel.CRITICAL, service.analyze(message));
    }

    // ===================== CRITICAL: Relapse + hopelessness combo =====================

    @Test
    void analyze_relapseAndHopelessness_returnsCritical() {
        assertEquals(CrisisLevel.CRITICAL,
                service.analyze("Jag har fått återfall och allt är hopplöst, ingen kan hjälpa"));
    }

    @Test
    void analyze_drinkingAgainAndGivingUp_returnsCritical() {
        assertEquals(CrisisLevel.CRITICAL,
                service.analyze("Jag har börjat igen och det är meningslöst att försöka"));
    }

    @Test
    void analyze_relapsePlusNothingWorks_returnsCritical() {
        assertEquals(CrisisLevel.CRITICAL,
                service.analyze("Druckit igen. Inget fungerar längre."));
    }

    // ===================== ELEVATED =====================

    @Test
    void analyze_relapse_returnsElevated() {
        assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag har fått ett återfall"));
    }

    @Test
    void analyze_drinkingAgain_returnsElevated() {
        assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag har börjat dricka igen"));
    }

    @Test
    void analyze_hopelessness_returnsElevated() {
        assertEquals(CrisisLevel.ELEVATED, service.analyze("Det känns helt hopplöst just nu"));
    }

    @Test
    void analyze_givingUp_returnsElevated() {
        assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag ger upp, kan inte mer"));
    }

    @Test
    void analyze_loneliness_returnsElevated() {
        assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag känner mig ensam, ingen förstår"));
    }

    @Test
    void analyze_panicAttack_returnsElevated() {
        assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag har panikångest just nu"));
    }

    @Test
    void analyze_anxietyAttack_returnsElevated() {
        assertEquals(CrisisLevel.ELEVATED, service.analyze("Har ångest attack"));
    }

    @Test
    void analyze_nobodyCares_returnsElevated() {
        assertEquals(CrisisLevel.ELEVATED, service.analyze("Ingen bryr sig om mig"));
    }

    @Test
    void analyze_cantCope_returnsElevated() {
        assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag klarar inte av det här"));
    }

    @Test
    void analyze_cravings_returnsElevated() {
        assertEquals(CrisisLevel.ELEVATED, service.analyze("Har stark craving just nu"));
    }

    @Test
    void analyze_abstinence_returnsElevated() {
        assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag har abstinens och mår illa"));
    }

    @Test
    void analyze_cravingSwedish_returnsElevated() {
        assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag har sug efter alkohol"));
    }

    // ===================== Case insensitivity =====================

    @Test
    void analyze_caseInsensitive_critical() {
        assertEquals(CrisisLevel.CRITICAL, service.analyze("SJÄLVMORD"));
    }

    @Test
    void analyze_caseInsensitive_overdose() {
        assertEquals(CrisisLevel.CRITICAL, service.analyze("ÖVERDOS"));
    }

    @Test
    void analyze_caseInsensitive_elevated() {
        assertEquals(CrisisLevel.ELEVATED, service.analyze("ÅTERFALL"));
    }

    // ===================== Crisis response =====================

    @Test
    void getCrisisResponse_containsPhoneNumbers() {
        String response = service.getCrisisResponse();
        assertTrue(response.contains("90101"), "Should contain Mind Självmordslinjen number");
        assertTrue(response.contains("112"), "Should contain emergency number");
        assertTrue(response.contains("08-702 16 80"), "Should contain Jourhavande medmänniska number");
    }

    @Test
    void getCrisisResponse_isNotEmpty() {
        assertFalse(service.getCrisisResponse().isBlank());
    }

    @Test
    void getCrisisResponse_containsMindReference() {
        assertTrue(service.getCrisisResponse().contains("mind.se"));
    }
}
