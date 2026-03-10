package se.sobriety.nextstep.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sobriety.nextstep.entity.CoachMessage;

import java.util.List;

public interface CoachMessageRepository extends JpaRepository<CoachMessage, Long> {

    List<CoachMessage> findBySessionIdOrderByTimestampAsc(Long sessionId);

    List<CoachMessage> findBySessionIdOrderByTimestampDesc(Long sessionId, Pageable pageable);

    List<CoachMessage> findBySession_SessionIdOrderByTimestampAsc(String sessionId);
}

