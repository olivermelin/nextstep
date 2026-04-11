package se.sobriety.nextstep.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.dto.FeedItemDto;
import se.sobriety.nextstep.dto.FeedPageDto;
import se.sobriety.nextstep.dto.FeedReactionDto;
import se.sobriety.nextstep.entity.*;
import se.sobriety.nextstep.repository.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hanterar det sociala aktivitetsflödet.
 * Feed-inlägg genereras automatiskt när användaren slutför challenges,
 * når streak-milstolpar, level-uppar eller vinner dueller.
 *
 * Sekretess: delning är OPT-IN (feedSharingEnabled = false som default).
 * Ingen känslig hälsodata (humör, anteckningar) exponeras i flödet.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ActivityFeedService {

    private static final int PAGE_SIZE = 20;
    private static final long MAX_CURSOR = Long.MAX_VALUE;

    // Streak-milstolpar som publiceras
    private static final List<Integer> STREAK_MILESTONES = List.of(7, 14, 30, 60, 90, 180, 365);

    private final FeedItemRepository feedItemRepository;
    private final FeedReactionRepository feedReactionRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserSettingsRepository userSettingsRepository;

    // ============================================================
    // Publicera feed-inlägg
    // ============================================================

    /**
     * Publicera att en challenge slutförts.
     * Anropas av UserChallengeService.completeChallenge().
     */
    @Transactional
    public void publishChallengeCompleted(String userId, UserChallenge uc) {
        // Ingen isSharingEnabled-check här — användaren har explicit valt att dela
        String displayName = getDisplayName(userId);
        String title       = uc.getChallenge().getTitle();
        String message     = displayName + " klarade \"" + title + "\"";
        String metadata    = "{\"points\":" + uc.getPointsEarned() +
                             ",\"category\":\"" + uc.getChallenge().getCategory().name() + "\"}";

        save(userId, FeedItemType.CHALLENGE_COMPLETED, uc.getId(), message, displayName, metadata);
    }

    /**
     * Publicera streak-milstolpe om dagantalet är en milstolpe.
     * Anropas av StreakService.registerActivity().
     */
    @Transactional
    public void publishStreakMilestoneIfReached(String userId, int currentStreak) {
        if (!STREAK_MILESTONES.contains(currentStreak)) return;
        if (!isSharingEnabled(userId, FeedItemType.STREAK_MILESTONE)) return;

        String displayName = getDisplayName(userId);
        String message     = displayName + " har nått " + currentStreak + " dagars streak!";
        String metadata    = "{\"streakDays\":" + currentStreak + "}";

        save(userId, FeedItemType.STREAK_MILESTONE, null, message, displayName, metadata);
    }

    /**
     * Publicera level-up.
     * Anropas av UserProgressService.addPoints() när nivå ökar.
     */
    @Transactional
    public void publishLevelUp(String userId, int newLevel) {
        if (!isSharingEnabled(userId, FeedItemType.LEVEL_UP)) return;

        String displayName = getDisplayName(userId);
        String message     = displayName + " nådde level " + newLevel + "!";
        String metadata    = "{\"level\":" + newLevel + "}";

        save(userId, FeedItemType.LEVEL_UP, null, message, displayName, metadata);
    }

    /**
     * Publicera duellvinst.
     * Anropas av DuelService.completeDuel() för vinnaren.
     */
    @Transactional
    public void publishDuelWon(String userId, Duel duel) {
        if (!isSharingEnabled(userId, FeedItemType.DUEL_WON)) return;

        String displayName = getDisplayName(userId);
        String message     = displayName + " vann en duell i \"" + duel.getChallengeName() + "\"!";
        String metadata    = "{\"duelId\":" + duel.getId() +
                             ",\"challenge\":\"" + duel.getChallengeName() + "\"}";

        save(userId, FeedItemType.DUEL_WON, duel.getId(), message, displayName, metadata);
    }

    // ============================================================
    // Hämta flödet
    // ============================================================

    /**
     * Hämtar paginerat flöde med inlägg från accepterade vänner.
     * cursorId = null innebär första sidan.
     */
    public FeedPageDto getFeed(String userId, Long cursorId) {
        // Hämta accepterade vänners userId
        List<String> friendIds = getFriendIds(userId);

        if (friendIds.isEmpty()) {
            return new FeedPageDto(Collections.emptyList(), null, false);
        }

        List<FeedItem> items;
        if (cursorId == null) {
            items = feedItemRepository.findFeedItemsFirstPage(
                    friendIds, PageRequest.of(0, PAGE_SIZE + 1));
        } else {
            items = feedItemRepository.findFeedItems(
                    friendIds, cursorId, PageRequest.of(0, PAGE_SIZE + 1));
        }

        boolean hasMore = items.size() > PAGE_SIZE;
        if (hasMore) items = items.subList(0, PAGE_SIZE);

        Long nextCursor = hasMore ? items.getLast().getId() : null;

        // Bygg upp DTO:er med reaktionsdata
        List<FeedItemDto> dtos = items.stream()
                .map(item -> toDto(item, userId))
                .collect(Collectors.toList());

        return new FeedPageDto(dtos, nextCursor, hasMore);
    }

    // ============================================================
    // Reaktioner (heja)
    // ============================================================

    /**
     * Växla heja-reaktion: lägg till om saknas, ta bort om finns.
     * Returnerar uppdaterat antal hejan.
     */
    @Transactional
    public Map<String, Object> toggleCheer(String userId, Long feedItemId) {
        // Verifiera att inlägget finns och att användaren är vän med ägaren
        FeedItem item = feedItemRepository.findById(feedItemId)
                .orElseThrow(() -> new IllegalArgumentException("Feed-inlägg hittades inte"));

        if (item.isDeleted()) {
            throw new IllegalArgumentException("Feed-inlägg är borttaget");
        }

        // Säkerhetskontroll: användaren måste vara vän med inläggets ägare
        if (!areFriends(userId, item.getUserId())) {
            throw new SecurityException("Åtkomst nekad");
        }

        String displayName = getDisplayName(userId);
        var existing = feedReactionRepository.findByUserIdAndFeedItemId(userId, feedItemId);

        boolean iCheered;
        if (existing.isPresent()) {
            feedReactionRepository.delete(existing.get());
            iCheered = false;
        } else {
            feedReactionRepository.save(new FeedReaction(userId, feedItemId, displayName));
            iCheered = true;
        }

        int cheerCount = feedReactionRepository.countByFeedItemId(feedItemId);
        return Map.of("cheerCount", cheerCount, "iCheered", iCheered);
    }

    // ============================================================
    // Hantering av egna inlägg
    // ============================================================

    /**
     * Soft-radera ett eget feed-inlägg.
     */
    @Transactional
    public void deleteFeedItem(String userId, Long feedItemId) {
        FeedItem item = feedItemRepository.findById(feedItemId)
                .orElseThrow(() -> new IllegalArgumentException("Feed-inlägg hittades inte"));

        if (!item.getUserId().equals(userId)) {
            throw new SecurityException("Du kan bara ta bort egna inlägg");
        }

        item.softDelete();
        feedItemRepository.save(item);
    }

    // ============================================================
    // GDPR
    // ============================================================

    /**
     * Exportera all feed-data för en användare.
     */
    public Map<String, Object> exportFeedData(String userId) {
        var items = feedItemRepository.findByUserIdAndDeletedFalse(userId)
                .stream()
                .map(item -> Map.of(
                        "type",        item.getType().name(),
                        "message",     item.getMessage(),
                        "createdAt",   item.getCreatedAt().toString(),
                        "cheerCount",  feedReactionRepository.countByFeedItemId(item.getId())
                ))
                .collect(Collectors.toList());

        var reactions = feedReactionRepository.findByUserId(userId)
                .stream()
                .map(r -> Map.of(
                        "feedItemId",  r.getFeedItemId(),
                        "createdAt",   r.getCreatedAt().toString()
                ))
                .collect(Collectors.toList());

        return Map.of("feedItems", items, "givenCheers", reactions);
    }

    /**
     * Radera all feed-data för en användare (GDPR).
     */
    @Transactional
    public void deleteAllForUser(String userId) {
        feedReactionRepository.deleteAllByUserId(userId);
        feedItemRepository.deleteAllByUserId(userId);
    }

    // ============================================================
    // Hjälpmetoder
    // ============================================================

    private void save(String userId, FeedItemType type, Long referenceId,
                      String message, String displayName, String metadata) {
        try {
            feedItemRepository.save(
                    new FeedItem(userId, type, referenceId, message, displayName, metadata));
        } catch (Exception e) {
            // Feed-generering ska aldrig avbryta den egentliga operationen
            log.warn("Kunde inte spara feed-inlägg för användare {}: {}", userId, e.getMessage());
        }
    }

    /** Kontrollerar om användaren har aktiverat feed-delning för den givna typen */
    private boolean isSharingEnabled(String userId, FeedItemType type) {
        return userSettingsRepository.findByUserId(userId)
                .map(s -> s.isFeedSharingEnabled() &&
                          (s.getFeedSharedTypes().isEmpty() || s.getFeedSharedTypes().contains(type)))
                .orElse(false);
    }

    /** Hämtar visningsnamn från inställningar (cachas i feed-inlägget) */
    private String getDisplayName(String userId) {
        return userSettingsRepository.findByUserId(userId)
                .map(UserSettings::getName)
                .filter(n -> n != null && !n.isBlank())
                .orElse("En användare");
    }

    /** Hämtar userId för alla accepterade vänner */
    private List<String> getFriendIds(String userId) {
        return friendshipRepository.findAcceptedFriendships(userId)
                .stream()
                .map(f -> f.getRequesterId().equals(userId) ? f.getAddresseeId() : f.getRequesterId())
                .distinct()
                .collect(Collectors.toList());
    }

    /** Kontrollerar om två användare är accepterade vänner */
    private boolean areFriends(String userId, String otherUserId) {
        return friendshipRepository.findBetween(userId, otherUserId)
                .map(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
                .orElse(false);
    }

    /** Bygger FeedItemDto med reaktionsdata */
    private FeedItemDto toDto(FeedItem item, String currentUserId) {
        int cheerCount    = feedReactionRepository.countByFeedItemId(item.getId());
        boolean iCheered  = feedReactionRepository
                .findByUserIdAndFeedItemId(currentUserId, item.getId())
                .isPresent();
        List<FeedReactionDto> recentCheers = feedReactionRepository
                .findTop3ByFeedItemIdOrderByCreatedAtDesc(item.getId())
                .stream()
                .map(r -> new FeedReactionDto(r.getId(), r.getDisplayName()))
                .collect(Collectors.toList());

        return new FeedItemDto(
                item.getId(),
                item.getUserId(),
                item.getDisplayName(),
                item.getType().name(),
                item.getMessage(),
                item.getMetadata(),
                item.getCreatedAt().toString(),
                cheerCount,
                iCheered,
                recentCheers
        );
    }
}
