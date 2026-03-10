package se.sobriety.nextstep.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.sobriety.nextstep.dto.MailRequest;
import se.sobriety.nextstep.service.mail.MailType;

/**
 * Exempel-service som visar hur man använder den förbättrade MailService.
 * Denna service kan utökas med fler mailmetoder allt efter behov.
 */
@Service
public class MailNotificationService {

    @Autowired
    private MailService mailService;

    /**
     * Skicka välkomstmail när användare registreras.
     */
    public void sendWelcomeEmail(String email, String firstName) {
        MailRequest request = MailRequest.builder()
                .type(MailType.WELCOME)
                .recipientEmail(email)
                .build();

        request.addVariable("firstName", firstName);

        mailService.sendMail(request);
    }

    /**
     * Skicka mail när användare uppnår en ny nivå.
     */
    public void sendLevelUpEmail(String email, String firstName, String level) {
        MailRequest request = MailRequest.builder()
                .type(MailType.LEVEL_UP)
                .recipientEmail(email)
                .build();

        request.addVariable("firstName", firstName);
        request.addVariable("level", level);

        mailService.sendMail(request);
    }

    /**
     * Skicka mail när användare låser upp en prestation.
     */
    public void sendAchievementUnlockedEmail(String email, String firstName, String achievementName) {
        MailRequest request = MailRequest.builder()
                .type(MailType.ACHIEVEMENT_UNLOCKED)
                .recipientEmail(email)
                .build();

        request.addVariable("firstName", firstName);
        request.addVariable("achievementName", achievementName);

        mailService.sendMail(request);
    }

    /**
     * Skicka veckovisa fortskrittsrapporter.
     */
    public void sendWeeklyProgressEmail(String email, String firstName, String week, String progress) {
        MailRequest request = MailRequest.builder()
                .type(MailType.WEEKLY_PROGRESS)
                .recipientEmail(email)
                .build();

        request.addVariable("firstName", firstName);
        request.addVariable("week", week);
        request.addVariable("progress", progress);

        mailService.sendMail(request);
    }

    /**
     * Skicka länk för återställning av lösenord.
     */
    public void sendPasswordResetEmail(String email, String resetLink) {
        MailRequest request = MailRequest.builder()
                .type(MailType.PASSWORD_RESET)
                .recipientEmail(email)
                .build();

        request.addVariable("resetLink", resetLink);

        mailService.sendMail(request);
    }
}
