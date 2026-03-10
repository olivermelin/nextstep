package se.sobriety.nextstep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sobriety.nextstep.service.mail.MailType;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO för mailförfrågningar. Används för att skicka mail med dynamiska variabler.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailRequest {
    private MailType type;
    private String recipientEmail;
    private String subject; // Valfritt - använder default från MailType om inte angiven

    @Builder.Default
    private Map<String, String> variables = new HashMap<>();

    /**
     * Lägg till en variabel för mailmallen.
     */
    public void addVariable(String key, String value) {
        this.variables.put(key, value);
    }
}
