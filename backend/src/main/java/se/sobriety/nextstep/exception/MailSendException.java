package se.sobriety.nextstep.exception;

/**
 * Exception som kastas när mail-skickande misslyckas.
 * Wrapper runt mer detaljerad error-information.
 */
public class MailSendException extends RuntimeException {

    private final String recipientEmail;
    private final String mailType;

    public MailSendException(String message, Throwable cause) {
        super(message, cause);
        this.recipientEmail = null;
        this.mailType = null;
    }

    public MailSendException(String message, String recipientEmail, String mailType, Throwable cause) {
        super(message, cause);
        this.recipientEmail = recipientEmail;
        this.mailType = mailType;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getMailType() {
        return mailType;
    }
}
