package se.sobriety.nextstep.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import se.sobriety.nextstep.dto.MailRequest;
import se.sobriety.nextstep.exception.MailSendException;
import se.sobriety.nextstep.service.mail.MailTemplate;
import se.sobriety.nextstep.service.mail.MailTemplateProvider;
import se.sobriety.nextstep.service.mail.MailType;

/**
 * Service för att skicka mail med stöd för templater och dynamiska variabler.
 * Hanterar både text och HTML-mail.
 */
@Service
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;
    private final MailTemplateProvider templateProvider;
    private final String senderEmail;

    @Autowired
    public MailService(JavaMailSender mailSender,
                      MailTemplateProvider templateProvider,
                      @Value("${mail.sender-email:noreply@nextstep.se}") String senderEmail) {
        this.mailSender = mailSender;
        this.templateProvider = templateProvider;
        this.senderEmail = senderEmail;
    }

    /**
     * Skicka mail baserat på en MailRequest.
     * @param request MailRequest med mailtyp, mottagare och variabler
     * @throws MailSendException om mail inte kan skickas
     */
    public void sendMail(MailRequest request) {
        try {
            // VIKTIGT: Klona template för att undvika mutation av cachad original
            MailTemplate template = templateProvider.getTemplate(request.getType()).deepCopy();

            // Lägg till användarens variabler
            template.getVariables().putAll(request.getVariables());

            // Ersätt alla variabler
            template.replaceVariables();

            // Använd custom subject om angiven, annars använd templatens
            String subject = request.getSubject() != null ?
                    request.getSubject() : template.getSubject();

            if (template.isHtml()) {
                sendHtmlMail(request.getRecipientEmail(), subject, template.getContent());
            } else {
                sendTextMail(request.getRecipientEmail(), subject, template.getContent());
            }

            log.info("Mail skickat till {} av typ {}", request.getRecipientEmail(), request.getType());
        } catch (MailSendException e) {
            throw e; // Re-throw custom exceptions
        } catch (Exception e) {
            log.error("Fel vid skicka mail till {}: {}", request.getRecipientEmail(), e.getMessage(), e);
            throw new MailSendException(
                    "Kunde inte skicka mail till " + request.getRecipientEmail(),
                    request.getRecipientEmail(),
                    request.getType().name(),
                    e
            );
        }
    }

    /**
     * Skicka enkelt text-mail.
     */
    private void sendTextMail(String to, String subject, String text) {
        SimpleMailMessage msg = new SimpleMailMessage();
        setupMessageHeaders(msg, to, subject);
        msg.setText(text);
        mailSender.send(msg);
    }

    /**
     * Skicka HTML-mail.
     */
    private void sendHtmlMail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        setupMessageHeaders(helper, to, subject);
        helper.setText(htmlContent, true); // true = isHtml

        mailSender.send(message);
    }

    /**
     * Gemensam setup för SimpleMailMessage headers.
     */
    private void setupMessageHeaders(SimpleMailMessage msg, String to, String subject) {
        msg.setFrom(senderEmail);
        msg.setTo(to);
        msg.setSubject(subject);
    }

    /**
     * Gemensam setup för MimeMessageHelper headers.
     */
    private void setupMessageHeaders(MimeMessageHelper helper, String to, String subject) throws MessagingException {
        helper.setFrom(senderEmail);
        helper.setTo(to);
        helper.setSubject(subject);
    }

    /**
     * Skicka test-mail (kompatibilitet med tidigare kod).
     * @deprecated Använd {@link #sendMail(MailRequest)} istället
     */
    @Deprecated(forRemoval = true)
    public void sendTestMail(String email) {
        MailRequest request = MailRequest.builder()
                .type(MailType.TEST_MAIL)
                .recipientEmail(email)
                .build();
        sendMail(request);
    }
}
