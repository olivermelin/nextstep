package se.sobriety.nextstep.service.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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

    // =====================================================================
    // NONE — Tomma och neutrala meddelanden
    // =====================================================================

    @Nested
    class NoneTests {

        @ParameterizedTest
        @NullAndEmptySource
        void nullOrEmpty_returnsNone(String message) {
            assertEquals(CrisisLevel.NONE, service.analyze(message));
        }

        @Test
        void blankString_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("   "));
        }

        @Test
        void normalPositiveMessage_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Jag vill ta en promenad idag"));
        }

        @Test
        void positiveRecovery_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Det har varit en bra vecka"));
        }

        @Test
        void celebratingProgress_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Jag har klarat 30 dagar nu!"));
        }

        @Test
        void neutralCheckIn_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Mår okej idag, lite trött men det är bra"));
        }

        @Test
        void talkingAboutOthers_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Min vän berättade om sin ångest"));
        }

        @Test
        void askingForAdvice_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Vad kan jag göra för att sova bättre?"));
        }

        @Test
        void motivationalMessage_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Jag tror att jag kan klara det här"));
        }

        @Test
        void shortNeutralMessage_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Hej!"));
        }

        @Test
        void singleWord_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Okej"));
        }

        @Test
        void exerciseMessage_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Sprang 5 km igår, kändes bra"));
        }

        @Test
        void sleepMessage_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Sov riktigt bra igår natt"));
        }

        @Test
        void gratitudeMessage_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Tacksam för stödet från familjen"));
        }

        @Test
        void planningMessage_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Ska träffa min terapeut på fredag"));
        }

        @Test
        void cookingMessage_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Lagade mat och det gick bra"));
        }

        @Test
        void weatherMessage_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Det är fint väder idag, ska gå ut"));
        }

        // Falska positiver — ord som liknar men INTE är kris
        @Test
        void diedOfLaughter_falsePositive_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Jag dog av skratt på det där skämtet"));
        }

        @Test
        void killItMeaning_falsePositive_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Du fixar det, kör hårt idag!"));
        }

        @Test
        void liveForSomething_falsePositive_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Jag lever för min familj"));
        }

        @Test
        void killingTime_falsePositive_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Dödar lite tid med att läsa"));
        }

        @Test
        void dieMentioned_inPositiveContext_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Filmen var så bra att man nästan vill dö av lycka"));
        }

        @Test
        void feelingTiredButOkay_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Jag är ganska trött idag men annars bra"));
        }

        @Test
        void longnessInPositiveContext_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Saknar min kompis som bor långt bort"));
        }

        @Test
        void medicationMentioned_neutralContext_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Kom ihåg att ta mina mediciner idag"));
        }

        @Test
        void therapyMentioned_positively_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Terapi hjälper verkligen, jag mår bättre"));
        }

        @Test
        void helpSeeking_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Söker hjälp och det känns rätt"));
        }

        @Test
        void feelingStrongToday_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Jag känner mig stark och motiverad idag"));
        }

        @Test
        void weekendPlans_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Ska se film med familjen i helgen"));
        }

        @Test
        void mentioningChallenges_metaphorically_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Livet är svårt men det ordnar sig"));
        }

        @Test
        void pastStruggle_resolvedPositively_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Hade det tufft förra månaden men nu är det bättre"));
        }

        @Test
        void numbersAndStats_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Dag 47 utan alkohol, rekord!"));
        }

        @Test
        void questionAboutMindfulness_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Har ni tips på bra andningsövningar?"));
        }

        @Test
        void thanksToApp_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Tack för att appen finns, den hjälper"));
        }

        @Test
        void sharingAchievement_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("Klarade vecko-utmaningen, supernöjd!"));
        }
    }

    // =====================================================================
    // CRITICAL — Direkta självmordsuttryck på svenska
    // =====================================================================

    @Nested
    class CriticalSwedishSuicideTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "Jag vill ta livet av mig",
                "Jag tänker begå självmord",
                "Jag planerar att döda mig",
                "Jag vill inte leva längre",
                "Alla har det bättre utan mig",
                "Jag orkar inte leva mer"
        })
        void directSuicideExpressions_returnsCritical(String message) {
            assertEquals(CrisisLevel.CRITICAL, service.analyze(message),
                    "Should detect CRITICAL for: " + message);
        }

        @Test
        void suicideKeywordAlone_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("självmord"));
        }

        @Test
        void suicideInSentence_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag funderar kring självmord just nu"));
        }

        @Test
        void suicidKeyword_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("suicid"));
        }

        @Test
        void wantToDie_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag vill dö"));
        }

        @Test
        void noMeaningToLive_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Det finns ingen mening att leva"));
        }

        @Test
        void betterWithoutMe_returnsActual() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Alla hade det bättre utan mig"));
        }

        @Test
        void planSuicide_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag planerar ta livet av mig"));
        }

        @Test
        void comingToEndEverything_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Ska avsluta allt snart"));
        }

        @Test
        void willKillMySelf_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag ska döda mig"));
        }

        @Test
        void thinkAboutEndingLife_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Tänker på att ta livet av mig hela tiden"));
        }

        @Test
        void cantLiveAnyMore_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag orkar inte leva mer"));
        }

        @Test
        void suicideWithContextBefore_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag mår så dåligt och tänker på självmord"));
        }

        @Test
        void suicideWithContextAfter_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Självmord är det enda jag tänker på"));
        }

        @Test
        void longMessageWithSuicide_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze(
                    "Har haft en jättesvår dag och mår hemskt. Jag vet inte vad jag ska göra. " +
                    "Tänker på självmord och det skrämmer mig."));
        }

        @Test
        void suicideUppercase_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("SJÄLVMORD"));
        }

        @Test
        void suicideLowercase_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("självmord"));
        }

        @Test
        void suicideMixedCase_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("SjälvMord"));
        }

        @Test
        void wantToDieUppercase_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("JAG VILL DÖ"));
        }
    }

    // =====================================================================
    // CRITICAL — Överdos
    // =====================================================================

    @Nested
    class CriticalOverdoseTests {

        @Test
        void overdoseSwedish_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag tog en överdos igår"));
        }

        @Test
        void overdoseSwedishAlone_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("överdos"));
        }

        @Test
        void overdoseEnglish_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("I took an overdose"));
        }

        @Test
        void overdoseEnglishAlone_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("overdose"));
        }

        @Test
        void tookOverdose_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Har tagit en överdos idag"));
        }

        @Test
        void tookTooMuch_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag har tagit för mycket"));
        }

        @Test
        void overdoseUppercase_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("ÖVERDOS"));
        }

        @Test
        void overdoseInContext_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag är rädd att jag har tagit en överdos"));
        }
    }

    // =====================================================================
    // CRITICAL — Självskada
    // =====================================================================

    @Nested
    class CriticalSelfHarmTests {

        @Test
        void wantToSelfHarm_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag vill skära mig"));
        }

        @Test
        void selfHarming_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag skadar mig själv"));
        }

        @Test
        void selfHarmKeyword_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Har problem med självskada"));
        }

        @Test
        void selfHarmAlone_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("självskada"));
        }

        @Test
        void selfHarmInLongMessage_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze(
                    "Mår inte bra och har börjat med självskada igen, vet inte vad jag ska göra"));
        }
    }

    // =====================================================================
    // CRITICAL — Engelska uttryck
    // =====================================================================

    @Nested
    class CriticalEnglishTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "I want to kill myself",
                "I want to end my life",
                "I want to die",
                "suicide",
                "kill myself",
                "end my life"
        })
        void englishCrisisExpressions_returnsCritical(String message) {
            assertEquals(CrisisLevel.CRITICAL, service.analyze(message));
        }

        @Test
        void englishSuicideInSwedishContext_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL,
                    service.analyze("Jag mår så dåligt, I want to kill myself"));
        }

        @Test
        void suicideEnglishAlone_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("suicide"));
        }

        @Test
        void suicideEnglishUppercase_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("SUICIDE"));
        }
    }

    // =====================================================================
    // CRITICAL — Återfall + hopplöshet (kombination)
    // =====================================================================

    @Nested
    class CriticalRelapsePlusHopelessnessTests {

        @Test
        void relapseAndHopelessness_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL,
                    service.analyze("Jag har fått återfall och allt är hopplöst, ingen kan hjälpa"));
        }

        @Test
        void drinkingAgainAndGivingUp_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL,
                    service.analyze("Jag har börjat igen och det är meningslöst att försöka"));
        }

        @Test
        void relapsePlusNothingWorks_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL,
                    service.analyze("Druckit igen. Inget fungerar längre."));
        }

        @Test
        void relapseAndNoPointInTrying_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL,
                    service.analyze("Har fått återfall, ingen idé att fortsätta försöka"));
        }

        @Test
        void usedDrugsAgainAndAllIsLost_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL,
                    service.analyze("Tagit droger igen och ger upp helt, allt är kört"));
        }

        @Test
        void relapseWithHopelessnessVariant_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL,
                    service.analyze("Druckit igen, det är hopplöst, inget fungerar och ingen kan hjälpa mig"));
        }

        @Test
        void relapsedAndNobodyCanHelp_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL,
                    service.analyze("Jag har börjat använda igen och ingen kan hjälpa mig, det är meningslöst"));
        }
    }

    // =====================================================================
    // ELEVATED — Återfall (utan hopplöshet)
    // =====================================================================

    @Nested
    class ElevatedRelapseTests {

        @Test
        void relapse_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag har fått ett återfall"));
        }

        @Test
        void drinkingAgain_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag har börjat dricka igen"));
        }

        @Test
        void relapseAlone_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("återfall"));
        }

        @Test
        void relapseEnglish_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("relaps"));
        }

        @Test
        void usedDrugsAgain_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag har tagit droger igen"));
        }

        @Test
        void startedUsingAgain_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Har börjat använda igen"));
        }

        @Test
        void relapseWithContext_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED,
                    service.analyze("Hade ett återfall igår kväll, mår dåligt"));
        }

        @Test
        void drunkAgain_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Druckit igen, skäms"));
        }
    }

    // =====================================================================
    // ELEVATED — Hopplöshet (utan återfall)
    // =====================================================================

    @Nested
    class ElevatedHopelessnessTests {

        @Test
        void hopelessness_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Det känns helt hopplöst just nu"));
        }

        @Test
        void givingUp_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag ger upp, kan inte mer"));
        }

        @Test
        void cantCope_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag klarar inte av det här"));
        }

        @Test
        void noMeaning_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Ingenting känns meningsfullt längre"));
        }

        @Test
        void cantDoItAnymore_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag kan inte mer, orkar inte"));
        }
    }

    // =====================================================================
    // ELEVATED — Ensamhet och isolering
    // =====================================================================

    @Nested
    class ElevatedLonelinessTests {

        @Test
        void loneliness_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag känner mig ensam, ingen förstår"));
        }

        @Test
        void nobodyCares_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Ingen bryr sig om mig"));
        }

        @Test
        void nobodyUnderstands_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Ingen förstår vad jag går igenom"));
        }

        @Test
        void lonelyAlone_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag är så ensam"));
        }
    }

    // =====================================================================
    // ELEVATED — Panik och ångest
    // =====================================================================

    @Nested
    class ElevatedPanicTests {

        @Test
        void panicAttack_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag har panikångest just nu"));
        }

        @Test
        void anxietyAttack_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Har ångest attack"));
        }

        @Test
        void panic_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag har panik"));
        }

        @Test
        void panicAlone_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("panikångest"));
        }
    }

    // =====================================================================
    // ELEVATED — Sug och abstinens
    // =====================================================================

    @Nested
    class ElevatedCravingTests {

        @Test
        void cravings_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Har stark craving just nu"));
        }

        @Test
        void abstinence_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag har abstinens och mår illa"));
        }

        @Test
        void cravingSwedish_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag har sug efter alkohol"));
        }

        @Test
        void cravingAlone_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("craving"));
        }

        @Test
        void mustHaveIt_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag måste ha det, klarar mig inte"));
        }

        @Test
        void abstinenceSymptoms_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Abstinens är otroligt jobbigt"));
        }

        @Test
        void hugeCraving_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Har ett enormt sug efter en drink"));
        }
    }

    // =====================================================================
    // Case-insensitivitet (å, ä, ö)
    // =====================================================================

    @Nested
    class CaseInsensitivityTests {

        @Test
        void criticalUppercase_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("SJÄLVMORD"));
        }

        @Test
        void criticalLowercase_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("självmord"));
        }

        @Test
        void criticalMixedCase_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("SjÄlvMoRd"));
        }

        @Test
        void overdoseUppercase_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("ÖVERDOS"));
        }

        @Test
        void relapseUppercase_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("ÅTERFALL"));
        }

        @Test
        void relapseCapitalized_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Återfall"));
        }

        @Test
        void cravingUppercase_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("CRAVING"));
        }

        @Test
        void suicidUppercase_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("SUICID"));
        }

        @Test
        void selfHarmUppercase_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("SJÄLVSKADA"));
        }
    }

    // =====================================================================
    // Interpunktion och extra blanksteg
    // =====================================================================

    @Nested
    class PunctuationAndWhitespaceTests {

        @Test
        void suicideWithExclamation_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag tänker på självmord!"));
        }

        @Test
        void suicideWithPeriod_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag tänker på självmord."));
        }

        @Test
        void suicideWithComma_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Självmord, det är vad jag tänker på"));
        }

        @Test
        void relapseWithExclamation_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Jag har fått ett återfall!"));
        }

        @Test
        void messageWithLeadingTrailingSpaces_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("  Jag vill ta livet av mig  "));
        }

        @Test
        void messageWithMultipleSpaces_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("Jag  vill  ta  livet  av  mig"));
        }

        @Test
        void relapseWithColon_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("återfall: det hände igår"));
        }
    }

    // =====================================================================
    // Längre meningar och kontext
    // =====================================================================

    @Nested
    class LongerMessageTests {

        @Test
        void longMessageWithCriticalAtEnd_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze(
                    "Jag har haft det jobbigt länge och mår väldigt dåligt. " +
                    "Familjeproblem, ekonomiska problem och på jobbet går det dåligt. " +
                    "Jag vill ta livet av mig."));
        }

        @Test
        void longMessageWithCriticalAtStart_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze(
                    "Jag tänker på självmord. Har haft det jobbigt länge och " +
                    "vet inte hur jag ska hantera allt."));
        }

        @Test
        void longMessageWithElevatedOnly_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze(
                    "Haft en riktigt jobbig dag, känner mig hopplös och vet inte " +
                    "vad jag ska ta mig till. Sov dåligt och är trött."));
        }

        @Test
        void mixedLangMessageCritical_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze(
                    "Mår inte bra alls. I want to die and jag orkar inte fortsätta."));
        }

        @Test
        void longNeutralMessage_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze(
                    "Hade en okej dag. Gick till gymmet på morgonen, åt frukost och " +
                    "jobbade hemifrån. Lite trött men det är bra. Ska försöka sova tidigt."));
        }

        @Test
        void longMessageAboutRecovery_positive_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze(
                    "Det här är dag 60 utan alkohol och jag är stolt. Det har inte alltid " +
                    "varit lätt men stödet härifrån har hjälpt enormt. Tack!"));
        }

        @Test
        void longRelapseMessageNoHopelessness_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze(
                    "Hade ett återfall i helgen. Mår dåligt över det men försöker komma " +
                    "vidare. Vet att det är en del av processen."));
        }
    }

    // =====================================================================
    // Krissvaret
    // =====================================================================

    @Nested
    class CrisisResponseTests {

        @Test
        void getCrisisResponse_containsMindNumber() {
            assertTrue(service.getCrisisResponse().contains("90101"),
                    "Should contain Mind Självmordslinjen number");
        }

        @Test
        void getCrisisResponse_containsEmergencyNumber() {
            assertTrue(service.getCrisisResponse().contains("112"),
                    "Should contain emergency number");
        }

        @Test
        void getCrisisResponse_containsJourhavande() {
            assertTrue(service.getCrisisResponse().contains("08-702 16 80"),
                    "Should contain Jourhavande medmänniska number");
        }

        @Test
        void getCrisisResponse_isNotEmpty() {
            assertFalse(service.getCrisisResponse().isBlank());
        }

        @Test
        void getCrisisResponse_containsMindSite() {
            assertTrue(service.getCrisisResponse().contains("mind.se"));
        }

        @Test
        void getCrisisResponse_containsSupportiveLanguage() {
            String response = service.getCrisisResponse();
            assertTrue(response.contains("ensam") || response.contains("hjälp"),
                    "Should contain supportive language");
        }

        @Test
        void getCrisisResponse_isSwedish() {
            // Kontrollera att svaret innehåller svenska ord
            String response = service.getCrisisResponse();
            assertTrue(response.contains("Ring") || response.contains("kontakta"),
                    "Crisis response should be in Swedish");
        }
    }

    // =====================================================================
    // Gränsfall och edge cases
    // =====================================================================

    @Nested
    class EdgeCaseTests {

        @Test
        void singleSpaceOnly_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze(" "));
        }

        @Test
        void tabOnly_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("\t"));
        }

        @Test
        void newlineOnly_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("\n"));
        }

        @Test
        void veryLongNeutralMessage_returnsNone() {
            String longMessage = "Jag mår bra. ".repeat(100);
            assertEquals(CrisisLevel.NONE, service.analyze(longMessage));
        }

        @Test
        void veryLongMessageWithCrisis_returnsCritical() {
            String prefix = "Jag mår bra. ".repeat(50);
            assertEquals(CrisisLevel.CRITICAL, service.analyze(prefix + "Jag vill ta livet av mig"));
        }

        @Test
        void onlyPunctuation_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("...!!!???"));
        }

        @Test
        void onlyNumbers_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("12345"));
        }

        @Test
        void emojiOnly_returnsNone() {
            assertEquals(CrisisLevel.NONE, service.analyze("😊❤️🌟"));
        }

        @Test
        void criticalWordSurroundedByEmojis_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze("😔 självmord 😔"));
        }

        @Test
        void relapseWithEmoji_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze("Återfall 😢 mår dåligt"));
        }

        @Test
        void criticalWordAtVeryEnd_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL,
                    service.analyze("Har pratat med min coach men mår fortfarande dåligt, överdos"));
        }

        @Test
        void criticalWordAtVeryStart_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL,
                    service.analyze("Självmord är något jag tänkt på sedan länge"));
        }

        @Test
        void multiLineMessage_returnsCritical() {
            assertEquals(CrisisLevel.CRITICAL, service.analyze(
                    "Hej\nJag mår inte bra\nJag vill ta livet av mig"));
        }

        @Test
        void multiLineElevated_returnsElevated() {
            assertEquals(CrisisLevel.ELEVATED, service.analyze(
                    "Hej\nHade ett återfall\nMår dåligt"));
        }
    }
}
