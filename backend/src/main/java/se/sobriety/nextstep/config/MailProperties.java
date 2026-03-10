package se.sobriety.nextstep.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * Konfigurationsklass för mail-relaterade inställningar.
 * Kan läsas från application.yaml med prefix "mail".
 */
@Component
@ConfigurationProperties(prefix = "mail")
@Data
public class MailProperties {

    private Sender sender = new Sender();

    @Data
    public static class Sender {
        private String email = "noreply@nextstep.se";
        private String name = "NextStep";
    }
}
