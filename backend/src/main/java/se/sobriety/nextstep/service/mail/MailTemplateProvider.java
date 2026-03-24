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
                                <!DOCTYPE html>
                                <html lang="sv">
                                <head>
                                  <meta charset="UTF-8">
                                  <meta name="viewport" content="width=device-width,initial-scale=1.0">
                                  <title>Välkommen till NextStep</title>
                                </head>
                                <body style="margin:0;padding:0;background-color:#f2efe9;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;">
                                  <table width="100%" cellpadding="0" cellspacing="0" role="presentation">
                                    <tr>
                                      <td align="center" style="padding:40px 16px 48px;">

                                        <!-- Outer card -->
                                        <table width="100%" cellpadding="0" cellspacing="0" role="presentation" style="max-width:580px;">

                                          <!-- ── HEADER ── -->
                                          <tr>
                                            <td style="background-color:#16a34a;border-radius:20px 20px 0 0;padding:48px 48px 40px;text-align:center;">
                                              <!-- Logo pill -->
                                              <table cellpadding="0" cellspacing="0" role="presentation" style="margin:0 auto 28px;">
                                                <tr>
                                                  <td style="background:rgba(255,255,255,0.18);border-radius:50px;padding:8px 22px;">
                                                    <span style="color:#ffffff;font-size:18px;font-weight:700;letter-spacing:0.5px;">NextStep</span>
                                                  </td>
                                                </tr>
                                              </table>
                                              <!-- Icon circle -->
                                              <table cellpadding="0" cellspacing="0" role="presentation" style="margin:0 auto 24px;">
                                                <tr>
                                                  <td style="width:88px;height:88px;background:rgba(255,255,255,0.2);border-radius:50%;text-align:center;vertical-align:middle;font-size:40px;line-height:88px;">
                                                    &#127807;
                                                  </td>
                                                </tr>
                                              </table>
                                              <h1 style="margin:0 0 10px;color:#ffffff;font-size:30px;font-weight:700;line-height:1.2;">Välkommen, {{firstName}}!</h1>
                                              <p style="margin:0;color:rgba(255,255,255,0.82);font-size:16px;line-height:1.5;">Din resa mot ett bättre liv börjar just nu.</p>
                                            </td>
                                          </tr>

                                          <!-- ── BODY ── -->
                                          <tr>
                                            <td style="background-color:#ffffff;padding:44px 48px 36px;">

                                              <p style="margin:0 0 20px;color:#1a2e1a;font-size:16px;line-height:1.75;">Vi är verkligen glada att du har tagit detta steg. Återhämtning är en resa — och nu har du ett stöd vid din sida varje dag.</p>

                                              <p style="margin:0 0 28px;color:#4a6a4a;font-size:15px;line-height:1.6;font-weight:500;">Här är vad som väntar dig:</p>

                                              <!-- Feature: AI Coach -->
                                              <table width="100%" cellpadding="0" cellspacing="0" role="presentation" style="margin-bottom:14px;">
                                                <tr>
                                                  <td style="background-color:#f0fdf4;border-radius:12px;border-left:4px solid #16a34a;padding:18px 20px;">
                                                    <table cellpadding="0" cellspacing="0" role="presentation">
                                                      <tr>
                                                        <td style="font-size:26px;padding-right:16px;vertical-align:middle;">&#129302;</td>
                                                        <td>
                                                          <div style="color:#15803d;font-weight:700;font-size:14px;margin-bottom:4px;text-transform:uppercase;letter-spacing:0.5px;">AI Coach</div>
                                                          <div style="color:#374a37;font-size:14px;line-height:1.55;">Din personliga coach som lyssnar och stöttar dig dygnet runt.</div>
                                                        </td>
                                                      </tr>
                                                    </table>
                                                  </td>
                                                </tr>
                                              </table>

                                              <!-- Feature: Utmaningar -->
                                              <table width="100%" cellpadding="0" cellspacing="0" role="presentation" style="margin-bottom:14px;">
                                                <tr>
                                                  <td style="background-color:#eff6ff;border-radius:12px;border-left:4px solid #3b82f6;padding:18px 20px;">
                                                    <table cellpadding="0" cellspacing="0" role="presentation">
                                                      <tr>
                                                        <td style="font-size:26px;padding-right:16px;vertical-align:middle;">&#9889;</td>
                                                        <td>
                                                          <div style="color:#1d4ed8;font-weight:700;font-size:14px;margin-bottom:4px;text-transform:uppercase;letter-spacing:0.5px;">Dagliga utmaningar</div>
                                                          <div style="color:#2a3a5a;font-size:14px;line-height:1.55;">Bygg nya vanor med konkreta steg anpassade efter just dig.</div>
                                                        </td>
                                                      </tr>
                                                    </table>
                                                  </td>
                                                </tr>
                                              </table>

                                              <!-- Feature: Framsteg -->
                                              <table width="100%" cellpadding="0" cellspacing="0" role="presentation" style="margin-bottom:36px;">
                                                <tr>
                                                  <td style="background-color:#fefce8;border-radius:12px;border-left:4px solid #ca8a04;padding:18px 20px;">
                                                    <table cellpadding="0" cellspacing="0" role="presentation">
                                                      <tr>
                                                        <td style="font-size:26px;padding-right:16px;vertical-align:middle;">&#127942;</td>
                                                        <td>
                                                          <div style="color:#92400e;font-weight:700;font-size:14px;margin-bottom:4px;text-transform:uppercase;letter-spacing:0.5px;">Framsteg &amp; belöningar</div>
                                                          <div style="color:#4a3a1a;font-size:14px;line-height:1.55;">Följ din utveckling och fira varje litet steg framåt.</div>
                                                        </td>
                                                      </tr>
                                                    </table>
                                                  </td>
                                                </tr>
                                              </table>

                                              <!-- Quote box -->
                                              <table width="100%" cellpadding="0" cellspacing="0" role="presentation" style="margin-bottom:36px;">
                                                <tr>
                                                  <td style="background-color:#f8f6f2;border-radius:12px;padding:22px 28px;text-align:center;">
                                                    <p style="margin:0;color:#5a7a5a;font-size:15px;line-height:1.7;font-style:italic;">"Varje stor resa börjar med ett enda steg.<br>Du har redan tagit ditt."</p>
                                                  </td>
                                                </tr>
                                              </table>

                                              <!-- CTA Button -->
                                              <table width="100%" cellpadding="0" cellspacing="0" role="presentation">
                                                <tr>
                                                  <td align="center">
                                                    <a href="https://nextstep.se" style="display:inline-block;background-color:#16a34a;color:#ffffff;text-decoration:none;font-size:16px;font-weight:600;padding:16px 52px;border-radius:50px;letter-spacing:0.2px;">Kom igång nu &#8594;</a>
                                                  </td>
                                                </tr>
                                              </table>

                                            </td>
                                          </tr>

                                          <!-- ── FOOTER ── -->
                                          <tr>
                                            <td style="background-color:#ede9e2;border-radius:0 0 20px 20px;padding:28px 48px;text-align:center;border-top:1px solid #ddd8cf;">
                                              <p style="margin:0 0 6px;color:#7a8a7a;font-size:13px;line-height:1.6;">Du fick det här mailet för att du skapade ett konto på NextStep.</p>
                                              <p style="margin:0;color:#9aaa9a;font-size:12px;">NextStep &bull; Din partner i återhämtning</p>
                                            </td>
                                          </tr>

                                        </table>
                                      </td>
                                    </tr>
                                  </table>
                                </body>
                                </html>
                                """)
                        .textContent("""
                                Välkommen, {{firstName}}!

                                Vi är verkligen glada att du har tagit detta steg. Återhämtning är en resa — och nu har du ett stöd vid din sida varje dag.

                                Här är vad som väntar dig i NextStep:

                                AI Coach
                                Din personliga coach som lyssnar och stöttar dig dygnet runt.

                                Dagliga utmaningar
                                Bygg nya vanor med konkreta steg anpassade efter just dig.

                                Framsteg & belöningar
                                Följ din utveckling och fira varje litet steg framåt.

                                "Varje stor resa börjar med ett enda steg. Du har redan tagit ditt."

                                Kom igång: https://nextstep.se

                                ---
                                NextStep - Din partner i återhämtning
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
