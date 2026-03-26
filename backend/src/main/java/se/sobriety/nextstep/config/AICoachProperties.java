package se.sobriety.nextstep.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ai.coach")
@EnableConfigurationProperties
@Getter
@Setter
public class AICoachProperties {

    private boolean enabled = true;
    private int maxTokens = 500;
    private double temperature = 0.7;

    // Anthropic Claude settings
    private String anthropicApiKey;
    private String claudeModel = "claude-haiku-4-5";

    // Groq settings (gratis under utveckling)
    private boolean useGroq = false;
    private String groqApiKey;
    private String groqModel = "llama-3.1-8b-instant";
}
