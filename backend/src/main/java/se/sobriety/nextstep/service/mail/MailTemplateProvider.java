package se.sobriety.nextstep.service.mail;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * Provider för mailmallar. Kan utökas för att läsa mallar från fil, databas, etc.
 * För nu är mallarna hårdkodade men kan enkelt externaliseras.
 */
@Component
public class MailTemplateProvider {

    private final Map<String, MailTemplate> templates = new HashMap<>();

    public MailTemplateProvider() {
        initializeTemplates();
    }

    /**
     * Initialisera de fördefinierade mailmallarna.
     * Dessa kan senare läsas från en konfigurationsfil eller databas.
     */
    private void initializeTemplates() {
        templates.put(MailType.WELCOME.getTemplateId(),
                MailTemplate.builder()
                        .subject("Välkommen till NextStep, {{firstName}}!")
                        .htmlContent("""
                                <html>
                                <body style="font-family: Arial, sans-serif;">
                                    <h1>Välkommen, {{firstName}}!</h1>
                                    <p>Vi är glada att du har registrerat dig på NextStep.</p>
                                    <p>Din resan mot en nyktrare livsstil börjar här!</p>
                                    <p>Lycka till!</p>
                                </body>
                                </html>
                                """)
                        .textContent("""
                                Välkommen, {{firstName}}!
                                
                                Vi är glada att du har registrerat dig på NextStep.
                                Din resan mot en nyktrare livsstil börjar här!
                                
                                Lycka till!
                                """)
                        .isHtml(true)
                        .build());

        templates.put(MailType.LEVEL_UP.getTemplateId(),
                MailTemplate.builder()
                        .subject("Grattis {{firstName}}! Du har nått nivå {{level}}")
                        .htmlContent("""
                                <html>
                                <body style="font-family: Arial, sans-serif;">
                                    <h1>🎉 Grattis!</h1>
                                    <p>Du har nått nivå {{level}}</p>
                                    <p>Din hårda arbete betalar sig!</p>
                                </body>
                                </html>
                                """)
                        .textContent("""
                                🎉 Grattis!
                                
                                Du har nått nivå {{level}}
                                Din hårda arbete betalar sig!
                                """)
                        .isHtml(true)
                        .build());

        templates.put(MailType.ACHIEVEMENT_UNLOCKED.getTemplateId(),
                MailTemplate.builder()
                        .subject("Du har låst upp: {{achievementName}}")
                        .htmlContent("""
                                <html>
                                <body style="font-family: Arial, sans-serif;">
                                    <h1>🏆 Prestation Låst Upp!</h1>
                                    <p>{{achievementName}}</p>
                                    <p>Bra jobbat {{firstName}}!</p>
                                </body>
                                </html>
                                """)
                        .textContent("""
                                🏆 Prestation Låst Upp!
                                
                                {{achievementName}}
                                Bra jobbat {{firstName}}!
                                """)
                        .isHtml(true)
                        .build());

        templates.put(MailType.WEEKLY_PROGRESS.getTemplateId(),
                MailTemplate.builder()
                        .subject("Din veckovisa rapport - {{week}}")
                        .htmlContent("""
                                <html>
                                <body style="font-family: Arial, sans-serif;">
                                    <h1>Din Veckovisa Framgång</h1>
                                    <p>Hej {{firstName}},</p>
                                    <p>Här är din veckovisa rapport för vecka {{week}}:</p>
                                    <p><strong>Framsteg:</strong> {{progress}}</p>
                                    <p>Fortsätt med det bra arbetet!</p>
                                </body>
                                </html>
                                """)
                        .textContent("""
                                Din Veckovisa Framgång
                                
                                Hej {{firstName}},
                                
                                Här är din veckovisa rapport för vecka {{week}}:
                                Framsteg: {{progress}}
                                
                                Fortsätt med det bra arbetet!
                                """)
                        .isHtml(true)
                        .build());

        templates.put(MailType.PASSWORD_RESET.getTemplateId(),
                MailTemplate.builder()
                        .subject("Återställ ditt lösenord")
                        .htmlContent("""
                                <html>
                                <body style="font-family: Arial, sans-serif;">
                                    <h1>Återställ Ditt Lösenord</h1>
                                    <p>Du har begärt att återställa ditt lösenord.</p>
                                    <p><a href="{{resetLink}}">Klicka här för att återställa</a></p>
                                    <p>Denna länk är giltig i 24 timmar.</p>
                                </body>
                                </html>
                                """)
                        .textContent("""
                                Återställ Ditt Lösenord
                                
                                Du har begärt att återställa ditt lösenord.
                                Besök denna länk: {{resetLink}}
                                
                                Denna länk är giltig i 24 timmar.
                                """)
                        .isHtml(true)
                        .build());

        templates.put(MailType.TEST_MAIL.getTemplateId(),
                MailTemplate.builder()
                        .subject("Test")
                        .htmlContent("<html><body>Mail funkar 🎉</body></html>")
                        .textContent("Mail funkar 🎉")
                        .isHtml(true)
                        .build());
    }

    /**
     * Hämta en mailmall baserat på MailType.
     * @param type MailType för den mallen som ska hämtas
     * @return En kopia av templaten som kan modifieras
     * @throws IllegalArgumentException om malltypen inte finns
     */
    public MailTemplate getTemplate(MailType type) {
        MailTemplate template = templates.get(type.getTemplateId());
        if (template == null) {
            throw new IllegalArgumentException("Ingen mailmall finns för typ: " + type.getTemplateId());
        }
        // Returnera en kopia för att undvika att originalet modifieras
        return MailTemplate.builder()
                .subject(template.getSubject())
                .htmlContent(template.getHtmlContent())
                .textContent(template.getTextContent())
                .isHtml(template.isHtml())
                .variables(new HashMap<>(template.getVariables()))
                .build();
    }

    /**
     * Registrera en anpassad mailmall.
     */
    public void registerTemplate(MailType type, MailTemplate template) {
        templates.put(type.getTemplateId(), template);
    }
}
