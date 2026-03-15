package se.sobriety.nextstep.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.sobriety.nextstep.dto.UserSettingsInDto;
import se.sobriety.nextstep.dto.UserSettingsOutDto;
import se.sobriety.nextstep.service.UserSettingsService;

@RestController
@RequestMapping("/api/settings")
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    public UserSettingsController(UserSettingsService userSettingsService) {
        this.userSettingsService = userSettingsService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<UserSettingsOutDto> updateUserSettings(
            @PathVariable String userId,
            @Valid @RequestBody UserSettingsInDto dto) {

        UserSettingsOutDto updated = userSettingsService.updateUserSettings(userId, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserSettingsOutDto> getUserSettings(
            @PathVariable String userId) {

        UserSettingsOutDto settings = userSettingsService.getUserSettings(userId);
        return ResponseEntity.ok(settings);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserSettings(
            @PathVariable String userId) {

        userSettingsService.deleteUserSettings(userId);
        return ResponseEntity.noContent().build(); // 204 OK, tom body
    }
}
