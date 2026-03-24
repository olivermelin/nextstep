package se.sobriety.nextstep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sobriety.nextstep.entity.CoachSession;
import se.sobriety.nextstep.entity.SessionStatus;

import java.util.List;
import java.util.Optional;

public interface CoachSessionRepository extends JpaRepository<CoachSession, Long> {

    Optional<CoachSession> findBySessionId(String sessionId);

    Optional<CoachSession> findByUserIdAndStatus(String userId, SessionStatus status);

    List<CoachSession> findByUserIdOrderByCreatedAtDesc(String userId);

    void deleteByUserId(String userId);
}

