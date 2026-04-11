package se.sobriety.nextstep.controller;

import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.sobriety.nextstep.dto.UserSettingsInDto;
import se.sobriety.nextstep.dto.UserSettingsOutDto;
import se.sobriety.nextstep.service.GdprExportService;
import se.sobriety.nextstep.service.UserSettingsService;

import java.util.Map;

import static se.sobriety.nextstep.util.SecurityUtils.verifyUserAccess;

@RestController
@RequestMapping("/api/settings")
public class UserSettingsController {

    private final UserSettingsService userSettingsService;
    private final GdprExportService gdprExportService;

    public UserSettingsController(UserSettingsService userSettingsService,
                                   GdprExportService gdprExportService) {
        this.userSettingsService = userSettingsService;
        this.gdprExportService = gdprExportService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<UserSettingsOutDto> updateUserSettings(
            @PathVariable String userId,
            @Valid @RequestBody UserSettingsInDto dto) {

        verifyUserAccess(userId);
        UserSettingsOutDto updated = userSettingsService.updateUserSettings(userId, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserSettingsOutDto> getUserSettings(
            @PathVariable String userId) {

        verifyUserAccess(userId);
        UserSettingsOutDto settings = userSettingsService.getUserSettings(userId);
        return ResponseEntity.ok(settings);
    }

    /**
     * GET /api/settings/{userId}/export
     * GDPR Artikel 20 — returnerar all persondata som JSON-fil.
     */
    @GetMapping("/{userId}/export")
    public ResponseEntity<Map<String, Object>> exportUserData(@PathVariable String userId) {
        verifyUserAccess(userId);
        Map<String, Object> data = gdprExportService.exportUserData(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("nextstep-data.json").build()
        );

        return ResponseEntity.ok().headers(headers).body(data);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserSettings(
            @PathVariable String userId) {

        verifyUserAccess(userId);
        userSettingsService.deleteUserSettings(userId);
        return ResponseEntity.noContent().build(); // 204 OK, tom body
    }
}
