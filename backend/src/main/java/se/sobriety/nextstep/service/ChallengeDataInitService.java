package se.sobriety.nextstep.service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.entity.Challenge;
import se.sobriety.nextstep.entity.ChallengeCategory;
import se.sobriety.nextstep.entity.ChallengeDifficulty;
import se.sobriety.nextstep.repository.ChallengeRepository;
import se.sobriety.nextstep.repository.UserChallengeRepository;

import java.util.List;

/**
 * Service för att ladda initiala challenges i databasen
 */
@Service
public class ChallengeDataInitService {

    private static final Logger log = LoggerFactory.getLogger(ChallengeDataInitService.class);

    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final EntityManager entityManager;

    public ChallengeDataInitService(ChallengeRepository challengeRepository,
                                   UserChallengeRepository userChallengeRepository,
                                   EntityManager entityManager) {
        this.challengeRepository = challengeRepository;
        this.userChallengeRepository = userChallengeRepository;
        this.entityManager = entityManager;
    }

    @PostConstruct
    @Transactional
    public void initializeChallenges() {
        // Ta bort gammal check constraint som blockerar nya enum-värden
        try {
            entityManager.createNativeQuery(
                    "ALTER TABLE challenge DROP CONSTRAINT IF EXISTS challenge_category_check"
            ).executeUpdate();
            log.info("Tog bort gammal challenge_category_check constraint");
        } catch (Exception e) {
            log.warn("Kunde inte ta bort challenge_category_check: {}", e.getMessage());
        }

        // Rensa trasig user_challenge-data från tidigare omstarter
        long userChallengeCount = userChallengeRepository.count();
        if (userChallengeCount > 0) {
            log.info("Rensar {} trasiga user_challenge-rader", userChallengeCount);
            userChallengeRepository.deleteAll();
        }

        // Om databasen är helt tom, lägg in alla challenges
        if (challengeRepository.count() == 0) {
            challengeRepository.saveAll(getAllChallenges());
            log.info("Skapade {} challenges i tom databas", getAllChallenges().size());
            return;
        }

        // Lägg till challenges för kategorier som saknar data
        for (ChallengeCategory category : ChallengeCategory.values()) {
            if (challengeRepository.findByCategory(category).isEmpty()) {
                List<Challenge> newChallenges = getAllChallenges().stream()
                        .filter(c -> c.getCategory() == category)
                        .toList();
                if (!newChallenges.isEmpty()) {
                    challengeRepository.saveAll(newChallenges);
                    log.info("Skapade {} challenges för kategori {}", newChallenges.size(), category);
                }
            }
        }
    }

    private List<Challenge> getAllChallenges() {
        return List.of(
                // ==================== MENTAL HÄLSA ====================
                createChallenge(
                        "Mindfulness Meditation",
                        "En guidad meditation för att öka närvaro och minska stress. Fokusera på din andning och låt tankarna komma och gå utan att döma dem.",
                        10,
                        ChallengeDifficulty.EASY,
                        ChallengeCategory.MENTAL_HEALTH,
                        "https://www.youtube.com/watch?v=inpok4MKVLM",
                        "1. Hitta en bekväm sittposition\n2. Stäng ögonen\n3. Fokusera på din andning\n4. När tankarna vandrar, för försiktigt tillbaka uppmärksamheten till andetagen"
                ),
                createChallenge(
                        "Andningsövningar",
                        "Djupandning för att lugna nervsystemet. Box breathing-teknik: andas in, håll, andas ut, håll - alla i 4 sekunder.",
                        5,
                        ChallengeDifficulty.EASY,
                        ChallengeCategory.MENTAL_HEALTH,
                        "https://www.youtube.com/watch?v=tEmt1Znux58",
                        "1. Andas in genom näsan i 4 sekunder\n2. Håll andan i 4 sekunder\n3. Andas ut genom munnen i 4 sekunder\n4. Håll andan i 4 sekunder\n5. Upprepa 5 gånger"
                ),
                createChallenge(
                        "Dagbok Skrivning",
                        "Skriv ner dina tankar och känslor i en dagbok. En kraftfull metod för självreflektion och känslomässig bearbetning.",
                        15,
                        ChallengeDifficulty.MEDIUM,
                        ChallengeCategory.MENTAL_HEALTH,
                        null,
                        "1. Hitta en lugn plats\n2. Skriv fritt i 15 minuter\n3. Fokusera på dagens händelser och dina känslor\n4. Ingen behöver läsa detta - skriv ärligt"
                ),
                createChallenge(
                        "Gratitude Journal",
                        "Skriv ner 3 saker du är tacksam för idag. Forskning visar att tacksamhet ökar lycka och välmående.",
                        10,
                        ChallengeDifficulty.EASY,
                        ChallengeCategory.MENTAL_HEALTH,
                        null,
                        "1. Tänk på din dag\n2. Identifiera 3 saker du är tacksam för\n3. Skriv ner dem och varför\n4. Reflektera över känslan av tacksamhet"
                ),

                // ==================== FYSISK AKTIVITET ====================
                createChallenge(
                        "Morgonpromenad",
                        "En 30 minuters promenad i frisk luft för att starta dagen rätt. Ökar energi och förbättrar humöret.",
                        30,
                        ChallengeDifficulty.EASY,
                        ChallengeCategory.PHYSICAL_ACTIVITY,
                        null,
                        "1. Ta på dig bekväma skor\n2. Gå ut och promenera i minst 30 minuter\n3. Fokusera på omgivningen\n4. Andas djupt och njut av luften"
                ),
                createChallenge(
                        "HIIT Workout",
                        "Högintensiv intervallträning för maximal kaloriförbränning och konditionsförbättring på kort tid.",
                        20,
                        ChallengeDifficulty.HARD,
                        ChallengeCategory.PHYSICAL_ACTIVITY,
                        "https://www.youtube.com/watch?v=ml6cT4AZdqI",
                        "1. Värm upp i 3 minuter\n2. 30 sekunder max intensitet\n3. 30 sekunder vila\n4. Upprepa 15 gånger\n5. Nedvarvning i 2 minuter"
                ),
                createChallenge(
                        "Yoga Session",
                        "En komplett yoga-session för flexibilitet, styrka och mental balans. Perfekt för både kropp och själ.",
                        45,
                        ChallengeDifficulty.MEDIUM,
                        ChallengeCategory.PHYSICAL_ACTIVITY,
                        "https://www.youtube.com/watch?v=v7AYKMP6rOE",
                        "1. Rulla ut din yogamatta\n2. Följ videon\n3. Lyssna på din kropp\n4. Andas djupt genom hela passet"
                ),
                createChallenge(
                        "Stretching",
                        "En snabb stretch-rutin för att öka rörlighet och minska muskelspänningar. Perfekt efter långvarigt sittande.",
                        10,
                        ChallengeDifficulty.EASY,
                        ChallengeCategory.PHYSICAL_ACTIVITY,
                        "https://www.youtube.com/watch?v=g_tea8ZNk5A",
                        "1. Stretcha nacke och axlar\n2. Stretcha armar och handleder\n3. Stretcha rygg\n4. Stretcha ben och vader\n5. Håll varje stretch i 20-30 sekunder"
                ),

                // ==================== FOKUS & DISCIPLIN ====================
                createChallenge(
                        "Pomodoro Session",
                        "Arbeta fokuserat i 25 minuter utan distraktioner, följt av 5 minuters paus. En beprövad produktivitetsteknik.",
                        25,
                        ChallengeDifficulty.EASY,
                        ChallengeCategory.FOCUS_DISCIPLINE,
                        null,
                        "1. Välj en uppgift\n2. Sätt timer på 25 minuter\n3. Arbeta fokuserat utan distraktioner\n4. Ta 5 minuters paus\n5. Upprepa"
                ),
                createChallenge(
                        "Digital Detox",
                        "En timme helt utan mobil, dator eller tv. Tid för att vara närvarande och koppla av från skärmar.",
                        60,
                        ChallengeDifficulty.MEDIUM,
                        ChallengeCategory.FOCUS_DISCIPLINE,
                        null,
                        "1. Stäng av alla digitala enheter\n2. Lämna mobilen i ett annat rum\n3. Ägna dig åt en analog aktivitet\n4. Njut av tystnad och närvaro"
                ),
                createChallenge(
                        "Cold Shower",
                        "Avsluta din dusch med 2-3 minuter kallt vatten. Stärker mentalt och fysiskt, ökar energi och förbättrar immunförsvaret.",
                        5,
                        ChallengeDifficulty.MEDIUM,
                        ChallengeCategory.FOCUS_DISCIPLINE,
                        "https://www.youtube.com/watch?v=pq6WHJzOkno",
                        "1. Börja med vanlig dusch\n2. Sänk temperaturen gradvis\n3. Stå under kallt vatten i 2-3 minuter\n4. Andas lugnt och djupt\n5. Fokusera på att övervinna obehaget"
                ),
                createChallenge(
                        "No Social Media Day",
                        "En hel dag utan sociala medier. Perfekt för att bryta digitala beroenden och återfå mentalt utrymme.",
                        1440,
                        ChallengeDifficulty.HARD,
                        ChallengeCategory.FOCUS_DISCIPLINE,
                        null,
                        "1. Ta bort social media-appar från hemskärmen\n2. Logga ut från alla konton\n3. Undvik alla sociala medier i 24 timmar\n4. Notera hur du känner dig\n5. Reflektera över upplevelsen"
                ),

                // ==================== PERSONLIG UTVECKLING ====================
                createChallenge(
                        "Läs en Bok",
                        "Läs i 30 minuter från en bok som utvecklar dig. Kunskap är makt och läsning tränar hjärnan.",
                        30,
                        ChallengeDifficulty.EASY,
                        ChallengeCategory.PERSONAL_DEVELOPMENT,
                        null,
                        "1. Välj en bok som intresserar dig\n2. Hitta en lugn plats\n3. Läs fokuserat i 30 minuter\n4. Ta anteckningar om viktiga insikter"
                ),
                createChallenge(
                        "Lär Dig Något Nytt",
                        "Ägna en timme åt att lära dig en ny färdighet eller fördjupa kunskap inom ett område som intresserar dig.",
                        60,
                        ChallengeDifficulty.MEDIUM,
                        ChallengeCategory.PERSONAL_DEVELOPMENT,
                        null,
                        "1. Välj ett ämne som intresserar dig\n2. Använd YouTube, böcker eller onlinekurser\n3. Fokusera i 60 minuter\n4. Öva det du lärt dig\n5. Dokumentera dina lärdomar"
                ),
                createChallenge(
                        "Reflektera på Dagen",
                        "Avsluta dagen med 15 minuters reflektion. Vad gick bra? Vad kan förbättras? Hur växer du?",
                        15,
                        ChallengeDifficulty.EASY,
                        ChallengeCategory.PERSONAL_DEVELOPMENT,
                        null,
                        "1. Hitta en lugn stund\n2. Tänk på din dag\n3. Skriv svar på:\n   - Vad är jag stolt över idag?\n   - Vad kunde jag gjort bättre?\n   - Vad lärde jag mig?\n   - Hur kan jag växa imorgon?"
                ),

                // ==================== RITÖVNINGAR ====================
                createChallenge(
                        "Familjerelationer",
                        "Rita dina familjerelationer och hur de påverkar dig. Placera dig själv i mitten och dina familjemedlemmar runt dig – nära för starka band, långt bort för svaga.",
                        20,
                        ChallengeDifficulty.EASY,
                        ChallengeCategory.DRAWING_EXERCISES,
                        null,
                        "Material: Papper, färgpennor\n\n1. Rita dig själv i mitten\n2. Rita familjemedlemmar runt dig\n3. Nära = stark relation, långt bort = svag\n4. Dra linjer emellan: tjocka = starka band, streckade = osäkra\n5. Använd färger: grönt = stöd, rött = konflikt\n6. Diskutera med terapeuten: Vem står närmast? Vem vill du flytta närmare?"
                ),
                createChallenge(
                        "Familjen som djur",
                        "Rita varje familjemedlem som ett djur som representerar dem. En lekfull metod att utforska roller och relationer i familjen.",
                        25,
                        ChallengeDifficulty.EASY,
                        ChallengeCategory.DRAWING_EXERCISES,
                        null,
                        "Material: Papper, färgpennor\n\n1. Tänk på din familj – vilka djur liknar de?\n2. Rita varje person som sitt djur\n3. Placera djuren i en scen – vad gör de?\n4. Rita dig själv som ett djur bland dem\n5. Diskutera: Varför just de djuren? Hur samspelar de?"
                ),
                createChallenge(
                        "Veckokänslor",
                        "Rita hur din vecka har känts – använd färger och former. Varje dag får en egen ruta med en symbol eller färg som representerar dagens känsla.",
                        15,
                        ChallengeDifficulty.EASY,
                        ChallengeCategory.DRAWING_EXERCISES,
                        null,
                        "Material: Papper, färgpennor/kritor\n\n1. Rita 7 rutor i rad (mån–sön)\n2. Välj färger: rött = ilska, blått = sorg, gult = glädje, grönt = lugn\n3. Färglägg varje dag med den känsla som dominerade\n4. Skriv ett ord under varje ruta om varför\n5. Diskutera: Vilka mönster ser du? Vad utlöste de starkaste känslorna?"
                ),
                createChallenge(
                        "Känslokartan i kroppen",
                        "Rita en kroppskontur och färglägg var i kroppen du kände veckans känslor. Synliggör kopplingen mellan kropp och känsla.",
                        20,
                        ChallengeDifficulty.MEDIUM,
                        ChallengeCategory.DRAWING_EXERCISES,
                        null,
                        "Material: Stort papper, kritor\n\n1. Rita en stor kroppskontur\n2. Tänk igenom veckan: vilka starka känslor hade du?\n3. Välj en färg per känsla\n4. Färglägg den plats i kroppen där du kände känslan\n5. Skriv känslornas namn bredvid\n6. Diskutera: Var sitter stressen? Vad kan du göra när du känner det?"
                ),
                createChallenge(
                        "Självbild",
                        "Rita hur du ser på dig själv just nu. Två halvor: masken du visar utåt och ditt riktiga jag bakom den.",
                        25,
                        ChallengeDifficulty.MEDIUM,
                        ChallengeCategory.DRAWING_EXERCISES,
                        null,
                        "Material: Papper, färgpennor\n\n1. Dela pappret i två halvor\n2. Vänster: 'Jag utåt' – hur andra ser dig\n3. Höger: 'Jag inuti' – hur du egentligen mår\n4. Använd färger som förstärker känslan\n5. Diskutera: Varför bär du masken? Vem får se ditt riktiga jag?"
                ),
                createChallenge(
                        "Framtidsjaget",
                        "Rita dig själv om ett år – fri från beroendet. Hur ser du ut? Var är du? Vem är du med? Visualisera den framtid du jobbar mot.",
                        30,
                        ChallengeDifficulty.MEDIUM,
                        ChallengeCategory.DRAWING_EXERCISES,
                        null,
                        "Material: Papper, färgpennor\n\n1. Blunda och tänk: hur ser mitt liv ut om ett år?\n2. Rita bilden: var är du, vem är du med, vad gör du?\n3. Skriv tre ord som beskriver framtidsjaget\n4. Diskutera: Vad behövs för att nå dit? Vad är första steget?"
                ),
                createChallenge(
                        "Riskkartering",
                        "Rita situationer eller platser som triggar sug. Kartlägg dina riskmiljöer och identifiera mönster som du kan undvika eller hantera.",
                        30,
                        ChallengeDifficulty.MEDIUM,
                        ChallengeCategory.DRAWING_EXERCISES,
                        null,
                        "Material: Stort papper, pennor i olika färger\n\n1. Rita dig själv i mitten\n2. Runt dig: rita platser, personer och situationer som triggar sug\n3. Rött = hög risk, orange = medel, gult = låg\n4. Rita skyddande faktorer med grönt (t.ex. vänner, aktiviteter)\n5. Diskutera: Vilka triggers kan du undvika? Vilka måste du lära dig hantera?"
                ),
                createChallenge(
                        "Min trygga plats",
                        "Rita en plats där du känner dig trygg – verklig eller påhittad. Visualisera din fristad som du kan återvända till i tankarna vid stress eller sug.",
                        20,
                        ChallengeDifficulty.EASY,
                        ChallengeCategory.DRAWING_EXERCISES,
                        null,
                        "Material: Papper, kritor/färgpennor\n\n1. Blunda och tänk på en plats där du känner dig helt trygg\n2. Rita platsen med så mycket detalj du kan\n3. Lägg till färger, ljud och dofter som etiketter\n4. Rita dig själv i bilden – hur ser du ut där?\n5. Diskutera: Vad gör platsen trygg? Hur kan du besöka den i tankarna?"
                ),

                // ==================== HÄLSOSAMMA VANOR ====================
                createChallenge(
                        "Hälsosam Frukost",
                        "Börja dagen med en näringsrik frukost. God kost ger energi och stärker kropp och sinne i återhämtningen.",
                        20,
                        ChallengeDifficulty.EASY,
                        ChallengeCategory.HEALTHY_HABITS,
                        null,
                        "1. Planera en hälsosam frukost kvällen innan\n2. Inkludera protein, fibrer och frukt\n3. Ät lugnt utan skärmar\n4. Drick vatten till maten\n5. Notera hur du mår efteråt"
                ),
                createChallenge(
                        "Sömnrutin",
                        "Skapa en lugn kvällsrutin för bättre sömn. God sömn är grundläggande för återhämtning och mental hälsa.",
                        30,
                        ChallengeDifficulty.MEDIUM,
                        ChallengeCategory.HEALTHY_HABITS,
                        null,
                        "1. Stäng av alla skärmar 1 timme före sänggåendet\n2. Ta ett varmt bad eller dusch\n3. Läs en bok eller lyssna på lugn musik\n4. Skriv ner eventuella orostankar\n5. Gå och lägg dig vid samma tid varje kväll"
                ),
                createChallenge(
                        "Vattendrinking",
                        "Drick minst 8 glas vatten under dagen. Hydrering påverkar humör, energi och kognitiv funktion positivt.",
                        1440,
                        ChallengeDifficulty.EASY,
                        ChallengeCategory.HEALTHY_HABITS,
                        null,
                        "1. Fyll en vattenflaska på morgonen\n2. Drick ett glas vid varje måltid\n3. Sätt påminnelser varannan timme\n4. Byt ut sötade drycker mot vatten\n5. Notera din energinivå under dagen"
                ),
                createChallenge(
                        "Måltidsplanering",
                        "Planera hälsosamma måltider för hela veckan. Struktur och planering minskar stress och impulsiva val.",
                        45,
                        ChallengeDifficulty.MEDIUM,
                        ChallengeCategory.HEALTHY_HABITS,
                        null,
                        "1. Skriv ner vad du vill äta varje dag\n2. Gör en inköpslista\n3. Fokusera på varierad och näringsrik mat\n4. Förbered det du kan i förväg\n5. Planera in hälsosamma snacks"
                ),

                // ==================== SOCIALA FÄRDIGHETER ====================
                createChallenge(
                        "Samtalsövning",
                        "Starta ett samtal med någon du inte brukar prata med. Sociala kontakter stärker välmåendet och motverkar isolering.",
                        15,
                        ChallengeDifficulty.MEDIUM,
                        ChallengeCategory.SOCIAL_SKILLS,
                        null,
                        "1. Identifiera en person du vill prata med\n2. Förbered ett samtalsämne\n3. Starta samtalet med en öppen fråga\n4. Lyssna aktivt och visa intresse\n5. Reflektera efteråt: hur kändes det?"
                ),
                createChallenge(
                        "Tacka Någon",
                        "Uttryck genuin tacksamhet till någon som betytt mycket för dig. Att visa uppskattning stärker relationer.",
                        10,
                        ChallengeDifficulty.EASY,
                        ChallengeCategory.SOCIAL_SKILLS,
                        null,
                        "1. Tänk på någon som hjälpt dig\n2. Skriv ett meddelande eller ring\n3. Berätta specifikt vad du uppskattar\n4. Var ärlig och öppen\n5. Notera hur det känns att uttrycka tacksamhet"
                ),
                createChallenge(
                        "Gruppaktivitet",
                        "Delta i en gruppaktivitet eller ett event. Det kan vara en promenadgrupp, sportaktivitet eller hobbygrupp.",
                        60,
                        ChallengeDifficulty.HARD,
                        ChallengeCategory.SOCIAL_SKILLS,
                        null,
                        "1. Sök efter lokala grupper eller events\n2. Välj något som intresserar dig\n3. Anmäl dig och delta\n4. Försök prata med minst en ny person\n5. Reflektera: vill du delta igen?"
                ),
                createChallenge(
                        "Lyssna Aktivt",
                        "Öva på aktivt lyssnande i ett samtal. Fokusera helt på den andra personen utan att tänka på vad du ska svara.",
                        20,
                        ChallengeDifficulty.EASY,
                        ChallengeCategory.SOCIAL_SKILLS,
                        null,
                        "1. Välj ett samtal att öva i\n2. Ge full uppmärksamhet - lägg undan mobilen\n3. Ställ följdfrågor baserat på vad personen säger\n4. Sammanfatta vad du hört\n5. Undvik att ge råd om det inte efterfrågas"
                ),

                // ==================== EMOTIONELL MEDVETENHET ====================
                createChallenge(
                        "Känslocheck",
                        "Stanna upp tre gånger under dagen och identifiera vad du känner just nu. Namnge känslan utan att döma den.",
                        5,
                        ChallengeDifficulty.EASY,
                        ChallengeCategory.EMOTIONAL_AWARENESS,
                        null,
                        "1. Sätt tre påminnelser under dagen\n2. Stanna upp och andas\n3. Fråga dig: vad känner jag just nu?\n4. Namnge känslan (glad, ledsen, arg, orolig, etc.)\n5. Acceptera känslan utan att döma"
                ),
                createChallenge(
                        "Triggermapping",
                        "Identifiera och skriv ner dina emotionella triggers. Förståelse för vad som utlöser starka känslor är nyckeln till hantering.",
                        20,
                        ChallengeDifficulty.MEDIUM,
                        ChallengeCategory.EMOTIONAL_AWARENESS,
                        null,
                        "1. Tänk på senaste veckan\n2. Identifiera situationer som väckte starka känslor\n3. Skriv ner: situation, känsla, reaktion\n4. Sök mönster i dina triggers\n5. Brainstorma alternativa reaktioner"
                ),
                createChallenge(
                        "Känslodagbok",
                        "Skriv en detaljerad känslodagbok varje kväll. Dokumentera dina känslor, vad som orsakade dem och hur du hanterade dem.",
                        15,
                        ChallengeDifficulty.EASY,
                        ChallengeCategory.EMOTIONAL_AWARENESS,
                        null,
                        "1. Avsätt 15 minuter på kvällen\n2. Skriv ner de starkaste känslorna under dagen\n3. Beskriv vad som utlöste varje känsla\n4. Hur reagerade du?\n5. Vad kunde du gjort annorlunda?"
                ),
                createChallenge(
                        "Grounding-övning",
                        "Praktisera 5-4-3-2-1-tekniken för att jorda dig i nuet. En kraftfull övning vid ångest eller starka emotionella reaktioner.",
                        10,
                        ChallengeDifficulty.EASY,
                        ChallengeCategory.EMOTIONAL_AWARENESS,
                        "https://www.youtube.com/watch?v=30VMIEmA114",
                        "1. Identifiera 5 saker du kan SE\n2. Identifiera 4 saker du kan RÖRA\n3. Identifiera 3 saker du kan HÖRA\n4. Identifiera 2 saker du kan LUKTA\n5. Identifiera 1 sak du kan SMAKA"
                )
        );
    }

    private Challenge createChallenge(String title, String description, int durationMinutes,
                                     ChallengeDifficulty difficulty, ChallengeCategory category,
                                     String youtubeUrl, String instructions) {
        Challenge challenge = new Challenge(title, description, durationMinutes, difficulty, category);
        challenge.setYoutubeUrl(youtubeUrl);
        challenge.setInstructions(instructions);
        return challenge;
    }
}
