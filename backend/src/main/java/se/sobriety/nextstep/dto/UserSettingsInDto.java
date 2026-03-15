package se.sobriety.nextstep.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserSettingsInDto(
        String userId,

        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Email must be at most 255 characters")
        String email,

        @Size(max = 20, message = "Phone must be at most 20 characters")
        String phone,

        boolean notificationsEnabled,
        boolean aiNotificationsEnabled,
        boolean darkModeEnabled,

        @Size(max = 10, message = "Language code must be at most 10 characters")
        String language
) {
}
