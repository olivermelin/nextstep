package se.sobriety.nextstep.dto;

public record UserSettingsInDto(String userId, String name, String email, String phone,
                                boolean notificationsEnabled, boolean aiNotificationsEnabled,
                                boolean darkModeEnabled, String language) {
}

