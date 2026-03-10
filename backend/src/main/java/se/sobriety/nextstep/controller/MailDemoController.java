package se.sobriety.nextstep.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.sobriety.nextstep.service.MailNotificationService;

/**
 * Exempel-controller som visar hur man integrerar MailNotificationService
 * i befintliga endpoints för att skicka notifikationsmails.
 *
 * Detta är ett referensexempel och kan anpassas efter behov.
 */
@RestController
@RequestMapping("/api/mail-demo")
@Slf4j
public class MailDemoController {

    @Autowired
    private MailNotificationService mailNotificationService;

    /**
     * Skicka välkomstmail (exempel).
     */
    @PostMapping("/welcome")
    public ResponseEntity<String> sendWelcome(
            @RequestParam String email,
            @RequestParam String firstName) {
        try {
            mailNotificationService.sendWelcomeEmail(email, firstName);
            return ResponseEntity.ok("Välkomstmail skickat till " + email);
        } catch (Exception e) {
            log.error("Fel vid skicka välkomstmail", e);
            return ResponseEntity.status(500).body("Kunde inte skicka mail");
        }
    }

    /**
     * Skicka mail om nivåuppgradering (exempel).
     */
    @PostMapping("/level-up")
    public ResponseEntity<String> sendLevelUp(
            @RequestParam String email,
            @RequestParam String firstName,
            @RequestParam String level) {
        try {
            mailNotificationService.sendLevelUpEmail(email, firstName, level);
            return ResponseEntity.ok("Levelup-mail skickat till " + email);
        } catch (Exception e) {
            log.error("Fel vid skicka levelup-mail", e);
            return ResponseEntity.status(500).body("Kunde inte skicka mail");
        }
    }

    /**
     * Skicka prestatationsmail (exempel).
     */
    @PostMapping("/achievement")
    public ResponseEntity<String> sendAchievement(
            @RequestParam String email,
            @RequestParam String firstName,
            @RequestParam String achievementName) {
        try {
            mailNotificationService.sendAchievementUnlockedEmail(email, firstName, achievementName);
            return ResponseEntity.ok("Prestatationsmail skickat till " + email);
        } catch (Exception e) {
            log.error("Fel vid skicka prestatationsmail", e);
            return ResponseEntity.status(500).body("Kunde inte skicka mail");
        }
    }

    /**
     * Skicka veckovisa fortskrittsrapporter (exempel).
     */
    @PostMapping("/weekly-progress")
    public ResponseEntity<String> sendWeeklyProgress(
            @RequestParam String email,
            @RequestParam String firstName,
            @RequestParam String week,
            @RequestParam String progress) {
        try {
            mailNotificationService.sendWeeklyProgressEmail(email, firstName, week, progress);
            return ResponseEntity.ok("Veckorapport skickad till " + email);
        } catch (Exception e) {
            log.error("Fel vid skicka veckorapport", e);
            return ResponseEntity.status(500).body("Kunde inte skicka mail");
        }
    }

    /**
     * Skicka länk för lösenordsåterställning (exempel).
     */
    @PostMapping("/password-reset")
    public ResponseEntity<String> sendPasswordReset(
            @RequestParam String email,
            @RequestParam String resetLink) {
        try {
            mailNotificationService.sendPasswordResetEmail(email, resetLink);
            return ResponseEntity.ok("Återställningslänk skickad till " + email);
        } catch (Exception e) {
            log.error("Fel vid skicka återställningslänk", e);
            return ResponseEntity.status(500).body("Kunde inte skicka mail");
        }
    }
}
