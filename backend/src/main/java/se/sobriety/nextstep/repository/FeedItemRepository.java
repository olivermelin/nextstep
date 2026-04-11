package se.sobriety.nextstep.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.entity.FeedItem;

import java.util.List;

public interface FeedItemRepository extends JpaRepository<FeedItem, Long> {

    /** Cursor-baserat flöde: inlägg från vänner, före cursor-id, nyast först */
    @Query("SELECT fi FROM FeedItem fi WHERE fi.userId IN :friendIds " +
           "AND fi.deleted = false AND fi.id < :cursor ORDER BY fi.id DESC")
    List<FeedItem> findFeedItems(
            @Param("friendIds") List<String> friendIds,
            @Param("cursor") Long cursor,
            Pageable pageable);

    /** Första sidan (utan cursor) */
    @Query("SELECT fi FROM FeedItem fi WHERE fi.userId IN :friendIds " +
           "AND fi.deleted = false ORDER BY fi.id DESC")
    List<FeedItem> findFeedItemsFirstPage(
            @Param("friendIds") List<String> friendIds,
            Pageable pageable);

    /** Hämta alla inlägg för en användare (GDPR-export) */
    List<FeedItem> findByUserIdAndDeletedFalse(String userId);

    /** Radera alla inlägg för en användare (GDPR) */
    @Modifying
    @Transactional
    @Query("DELETE FROM FeedItem fi WHERE fi.userId = :userId")
    void deleteAllByUserId(@Param("userId") String userId);
}
