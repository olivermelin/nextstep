package se.sobriety.nextstep.service.mail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Representerar en mailmall med subject och innehål (HTML eller text).
 * Stöder dynamiska variabler som ersätts med faktiska värden.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailTemplate {
    private String subject;
    private String htmlContent;
    private String textContent;
    private boolean isHtml;

    @Builder.Default
    private Map<String, String> variables = new HashMap<>();

    /**
     * Ersätter alla variabler i templaten med värden från variables-mappen.
     * Variabler definieras med {{variabelNamn}} format.
     */
    public void replaceVariables() {
        variables.forEach((key, value) -> {
            String placeholder = "{{" + key + "}}";
            if (htmlContent != null) {
                htmlContent = htmlContent.replace(placeholder, value != null ? value : "");
            }
            if (textContent != null) {
                textContent = textContent.replace(placeholder, value != null ? value : "");
            }
            if (subject != null) {
                subject = subject.replace(placeholder, value != null ? value : "");
            }
        });
    }

    /**
     * Lägg till en variabel till templaten.
     */
    public void addVariable(String key, String value) {
        this.variables.put(key, value);
    }

    /**
     * Returnera innehållet (HTML eller text beroende på isHtml).
     */
    public String getContent() {
        return isHtml ? htmlContent : textContent;
    }

    /**
     * Skapa en djup kopia av denna template.
     * Viktigt: Prevents mutation av original-template som är cachad i MailTemplateProvider.
     */
    public MailTemplate deepCopy() {
        return MailTemplate.builder()
                .subject(this.subject)
                .htmlContent(this.htmlContent)
                .textContent(this.textContent)
                .isHtml(this.isHtml)
                .variables(new HashMap<>(this.variables))
                .build();
    }
}

