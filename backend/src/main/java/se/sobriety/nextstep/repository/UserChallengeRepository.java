package se.sobriety.nextstep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sobriety.nextstep.entity.UserChallenge;

import java.util.List;
import java.util.Optional;

public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {

    List<UserChallenge> findByUserId(String userId);

    List<UserChallenge> findByUserIdAndCompleted(String userId, boolean completed);

    Optional<UserChallenge> findByUserIdAndChallengeId(String userId, Long challengeId);

    boolean existsByUserIdAndChallengeIdAndCompleted(String userId, Long challengeId, boolean completed);
}

