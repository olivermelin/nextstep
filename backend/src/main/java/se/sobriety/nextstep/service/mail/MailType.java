package se.sobriety.nextstep.service.mail;

/**
 * Enum för olika mailtyper som stöds av systemet.
 * Varje mailtyp har en unik identifier som används för att hämta rätt template.
 */
public enum MailType {
    WELCOME("welcome", "Välkommen till NextStep"),
    LEVEL_UP("level_up", "Du har nått en ny nivå!"),
    ACHIEVEMENT_UNLOCKED("achievement_unlocked", "Du har låst upp en prestation!"),
    WEEKLY_PROGRESS("weekly_progress", "Din veckovisa framgång"),
    PASSWORD_RESET("password_reset", "Återställ ditt lösenord"),
    TEST_MAIL("test_mail", "Test");

    private final String templateId;
    private final String defaultSubject;

    MailType(String templateId, String defaultSubject) {
        this.templateId = templateId;
        this.defaultSubject = defaultSubject;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getDefaultSubject() {
        return defaultSubject;
    }
}
