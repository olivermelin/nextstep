package se.sobriety.nextstep.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${app.frontend-base-url:http://localhost:8082}")
    private String frontendBaseUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF - inaktiverat för API (stateless REST).
                // TODO: Aktivera CSRF med cookie-baserade tokens vid session-baserad auth
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))

                // Security Headers
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .contentTypeOptions(contentType -> {})
                        .cacheControl(cache -> {})
                )

                // Authorization - publika endpoints
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/public/**",
                                "/api/auth/login",
                                "/api/auth/signup",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/me",
                                "/api/auth/logout",
                                "/login/**",
                                "/oauth2/**"
                        ).permitAll()
                        // Alla andra API-endpoints kräver autentisering
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )

                // OAuth2
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl(frontendBaseUrl + "/")
                )

                .logout(logout -> logout
                        .logoutSuccessUrl(frontendBaseUrl + "/").permitAll()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(frontendBaseUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization", "Accept", "X-Requested-With"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
