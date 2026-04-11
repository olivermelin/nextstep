package se.sobriety.nextstep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.entity.FeedReaction;

import java.util.List;
import java.util.Optional;

public interface FeedReactionRepository extends JpaRepository<FeedReaction, Long> {

    /** Räkna reaktioner på ett inlägg */
    int countByFeedItemId(Long feedItemId);

    /** Hämta senaste N reaktioner på ett inlägg (för att visa namn) */
    List<FeedReaction> findTop3ByFeedItemIdOrderByCreatedAtDesc(Long feedItemId);

    /** Kolla om en användare redan hejar på ett inlägg */
    Optional<FeedReaction> findByUserIdAndFeedItemId(String userId, Long feedItemId);

    /** Hämta alla reaktioner en användare gett (GDPR-export) */
    List<FeedReaction> findByUserId(String userId);

    /** Radera alla reaktioner för en användare (GDPR) */
    @Modifying
    @Transactional
    @Query("DELETE FROM FeedReaction r WHERE r.userId = :userId")
    void deleteAllByUserId(@Param("userId") String userId);

    /** Radera alla reaktioner på en specifik post */
    @Modifying
    @Transactional
    void deleteAllByFeedItemId(Long feedItemId);
}
