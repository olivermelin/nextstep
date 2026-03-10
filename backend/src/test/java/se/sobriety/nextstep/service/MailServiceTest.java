package se.sobriety.nextstep.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import jakarta.mail.internet.MimeMessage;
import se.sobriety.nextstep.dto.MailRequest;
import se.sobriety.nextstep.service.mail.MailTemplateProvider;
import se.sobriety.nextstep.service.mail.MailType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test-exempel för MailService.
 * Visar hur man testar mailskickandet utan att faktiskt skicka mail.
 */
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private MailTemplateProvider templateProvider;
    private MailService mailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        templateProvider = new MailTemplateProvider();
        mailService = new MailService(mailSender, templateProvider, "test@nextstep.se");

        // Mock MimeMessage creation för HTML-mail
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
    }

    @Test
    void testSendWelcomeEmail() {
        // Given
        MailRequest request = MailRequest.builder()
                .type(MailType.WELCOME)
                .recipientEmail("user@example.com")
                .build();
        request.addVariable("firstName", "John");

        // When
        mailService.sendMail(request);

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendLevelUpEmail() {
        // Given
        MailRequest request = MailRequest.builder()
                .type(MailType.LEVEL_UP)
                .recipientEmail("user@example.com")
                .build();
        request.addVariable("firstName", "John");
        request.addVariable("level", "10");

        // When
        mailService.sendMail(request);

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendMailWithCustomSubject() {
        // Given
        MailRequest request = MailRequest.builder()
                .type(MailType.ACHIEVEMENT_UNLOCKED)
                .recipientEmail("user@example.com")
                .subject("Custom Subject")
                .build();
        request.addVariable("firstName", "John");
        request.addVariable("achievementName", "Runner");

        // When
        mailService.sendMail(request);

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendWithValidMailRequest() {
        // Given
        MailRequest request = MailRequest.builder()
                .type(MailType.WELCOME)
                .recipientEmail("user@example.com")
                .build();

        // When
        mailService.sendMail(request);

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
