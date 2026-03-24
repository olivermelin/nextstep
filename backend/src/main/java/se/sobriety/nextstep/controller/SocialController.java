package se.sobriety.nextstep.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.sobriety.nextstep.dto.*;
import se.sobriety.nextstep.service.DuelService;
import se.sobriety.nextstep.service.FriendshipService;
import se.sobriety.nextstep.service.LeagueService;
import se.sobriety.nextstep.util.SecurityUtils;

import java.util.List;
import java.util.Map;

/**
 * REST-controller för sociala funktioner: vänner, dueller och liga.
 */
@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialController {

    private final FriendshipService friendshipService;
    private final DuelService duelService;
    private final LeagueService leagueService;

    // ===================== VÄNNER =====================

    /** Skicka vänförfrågan */
    @PostMapping("/friends/request")
    public ResponseEntity<FriendDto> sendFriendRequest(
            @RequestParam String userId,
            @Valid @RequestBody SendFriendRequestDto request) {
        SecurityUtils.verifyUserAccess(userId);
        var result = friendshipService.sendRequest(userId, request.targetEmail());
        return ResponseEntity.ok(result);
    }

    /** Hämta vänlistan */
    @GetMapping("/friends")
    public ResponseEntity<List<FriendDto>> getFriends(@RequestParam String userId) {
        SecurityUtils.verifyUserAccess(userId);
        return ResponseEntity.ok(friendshipService.getFriends(userId));
    }

    /** Hämta inkommande väntande förfrågningar */
    @GetMapping("/friends/pending")
    public ResponseEntity<List<FriendDto>> getPendingRequests(@RequestParam String userId) {
        SecurityUtils.verifyUserAccess(userId);
        return ResponseEntity.ok(friendshipService.getPendingRequests(userId));
    }

    /** Acceptera vänförfrågan */
    @PostMapping("/friends/{friendshipId}/accept")
    public ResponseEntity<FriendDto> acceptFriendRequest(
            @PathVariable Long friendshipId,
            @RequestParam String userId) {
        SecurityUtils.verifyUserAccess(userId);
        return ResponseEntity.ok(friendshipService.respondToRequest(friendshipId, userId, true));
    }

    /** Avvisa vänförfrågan */
    @PostMapping("/friends/{friendshipId}/decline")
    public ResponseEntity<FriendDto> declineFriendRequest(
            @PathVariable Long friendshipId,
            @RequestParam String userId) {
        SecurityUtils.verifyUserAccess(userId);
        return ResponseEntity.ok(friendshipService.respondToRequest(friendshipId, userId, false));
    }

    /** Ta bort vän */
    @DeleteMapping("/friends/{friendshipId}")
    public ResponseEntity<Map<String, String>> removeFriend(
            @PathVariable Long friendshipId,
            @RequestParam String userId) {
        SecurityUtils.verifyUserAccess(userId);
        friendshipService.removeFriend(friendshipId, userId);
        return ResponseEntity.ok(Map.of("message", "Vänrelation borttagen"));
    }

    // ===================== DUELLER =====================

    /** Skapa en ny duell */
    @PostMapping("/duels")
    public ResponseEntity<DuelDto> createDuel(
            @RequestParam String userId,
            @Valid @RequestBody CreateDuelDto request) {
        SecurityUtils.verifyUserAccess(userId);
        return ResponseEntity.ok(duelService.createDuel(userId, request));
    }

    /** Hämta alla dueller för en användare */
    @GetMapping("/duels")
    public ResponseEntity<List<DuelDto>> getDuels(@RequestParam String userId) {
        SecurityUtils.verifyUserAccess(userId);
        return ResponseEntity.ok(duelService.getDuels(userId));
    }

    /** Acceptera en duell */
    @PostMapping("/duels/{duelId}/accept")
    public ResponseEntity<DuelDto> acceptDuel(
            @PathVariable Long duelId,
            @RequestParam String userId) {
        SecurityUtils.verifyUserAccess(userId);
        return ResponseEntity.ok(duelService.respondToDuel(duelId, userId, true));
    }

    /** Avvisa en duell */
    @PostMapping("/duels/{duelId}/decline")
    public ResponseEntity<DuelDto> declineDuel(
            @PathVariable Long duelId,
            @RequestParam String userId) {
        SecurityUtils.verifyUserAccess(userId);
        return ResponseEntity.ok(duelService.respondToDuel(duelId, userId, false));
    }

    /** Markera att man slutfört sin del av duellen */
    @PostMapping("/duels/{duelId}/complete")
    public ResponseEntity<DuelDto> completeDuel(
            @PathVariable Long duelId,
            @RequestParam String userId) {
        SecurityUtils.verifyUserAccess(userId);
        return ResponseEntity.ok(duelService.completeDuel(duelId, userId));
    }

    // ===================== LIGA =====================

    /** Hämta topplistan (top 50) */
    @GetMapping("/league")
    public ResponseEntity<List<LeagueEntryDto>> getLeaderboard(@RequestParam String userId) {
        SecurityUtils.verifyUserAccess(userId);
        return ResponseEntity.ok(leagueService.getLeaderboard(userId));
    }

    /** Hämta den inloggade användarens ligaposition */
    @GetMapping("/league/me")
    public ResponseEntity<LeagueEntryDto> getMyLeagueEntry(@RequestParam String userId) {
        SecurityUtils.verifyUserAccess(userId);
        return ResponseEntity.ok(leagueService.getUserLeagueEntry(userId));
    }

    // ===================== FELHANTERING =====================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(409).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(SecurityException ex) {
        return ResponseEntity.status(403).body(Map.of("error", ex.getMessage()));
    }
}
