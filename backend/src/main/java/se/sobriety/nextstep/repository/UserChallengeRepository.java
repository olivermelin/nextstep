package se.sobriety.nextstep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.sobriety.nextstep.entity.UserChallenge;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {

    List<UserChallenge> findByUserId(String userId);

    List<UserChallenge> findByUserIdAndCompleted(String userId, boolean completed);

    Optional<UserChallenge> findTopByUserIdAndChallengeIdAndCompletedOrderByStartedAtDesc(String userId, Long challengeId, boolean completed);

    /**
     * Hitta den senaste (aktiva eller slutförda) UserChallenge för en användare och challenge.
     */
    Optional<UserChallenge> findTopByUserIdAndChallengeIdOrderByStartedAtDesc(String userId, Long challengeId);

    boolean existsByUserIdAndChallengeIdAndCompleted(String userId, Long challengeId, boolean completed);

    /**
     * Kolla om en användare har slutfört en specifik challenge efter en viss tidpunkt (t.ex. dagens start)
     */
    @Query("SELECT CASE WHEN COUNT(uc) > 0 THEN true ELSE false END FROM UserChallenge uc " +
           "WHERE uc.userId = :userId AND uc.challenge.id = :challengeId " +
           "AND uc.completed = true AND uc.completedAt >= :since")
    boolean existsCompletedSince(@Param("userId") String userId,
                                 @Param("challengeId") Long challengeId,
                                 @Param("since") LocalDateTime since);

    /**
     * Hämta alla challenge-id:n som en användare har slutfört efter en viss tidpunkt
     */
    @Query("SELECT uc.challenge.id FROM UserChallenge uc " +
           "WHERE uc.userId = :userId AND uc.completed = true AND uc.completedAt >= :since")
    List<Long> findCompletedChallengeIdsSince(@Param("userId") String userId,
                                              @Param("since") LocalDateTime since);
}

