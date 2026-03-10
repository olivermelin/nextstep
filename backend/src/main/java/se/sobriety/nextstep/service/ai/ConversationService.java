package se.sobriety.nextstep.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.entity.*;
import se.sobriety.nextstep.repository.CoachMessageRepository;
import se.sobriety.nextstep.repository.CoachSessionRepository;

import java.util.*;

/**
 * Hanterar sessionslivscykel och meddelandepersistens för coach-konversationer.
 */
@Service
@Transactional
public class ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);
    private static final int MAX_HISTORY_MESSAGES = 20;

    private final CoachSessionRepository sessionRepository;
    private final CoachMessageRepository messageRepository;

    public ConversationService(CoachSessionRepository sessionRepository,
                               CoachMessageRepository messageRepository) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * Hämtar aktiv session för användaren, eller skapar en ny.
     */
    public CoachSession getOrCreateActiveSession(String userId) {
        return sessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE)
                .orElseGet(() -> {
                    log.info("Creating new coach session for user {}", userId);
                    CoachSession session = new CoachSession(userId);
                    return sessionRepository.save(session);
                });
    }

    /**
     * Hämtar en specifik session via sessionId.
     */
    public Optional<CoachSession> getSession(String sessionId) {
        return sessionRepository.findBySessionId(sessionId);
    }

    /**
     * Sparar ett meddelande i sessionen.
     */
    public CoachMessage saveMessage(CoachSession session, String role, String content) {
        CoachMessage message = new CoachMessage(session, role, content);
        session.touch();
        sessionRepository.save(session);
        return messageRepository.save(message);
    }

    /**
     * Sparar ett meddelande med krisflagga.
     */
    public CoachMessage saveMessageWithCrisis(CoachSession session, String role, String content, CrisisLevel crisisLevel) {
        CoachMessage message = new CoachMessage(session, role, content);
        message.flagCrisis(crisisLevel);
        session.touch();
        sessionRepository.save(session);
        return messageRepository.save(message);
    }

    /**
     * Bygger meddelandehistorik formaterad för Claude API.
     * Returnerar de senaste MAX_HISTORY_MESSAGES meddelandena som List<Map<String, String>>
     * med role/content-par.
     */
    public List<Map<String, String>> buildMessageHistory(String sessionId) {
        // Hämta session via sessionId-sträng
        Optional<CoachSession> sessionOpt = sessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isEmpty()) {
            return List.of();
        }

        CoachSession session = sessionOpt.get();

        // Hämta senaste meddelanden (desc), begränsa till MAX_HISTORY_MESSAGES
        List<CoachMessage> recentMessages = messageRepository
                .findBySessionIdOrderByTimestampDesc(session.getId(), PageRequest.of(0, MAX_HISTORY_MESSAGES));

        // Vänd ordningen till kronologisk (äldst först)
        List<CoachMessage> chronological = new ArrayList<>(recentMessages);
        Collections.reverse(chronological);

        return chronological.stream()
                .map(msg -> Map.of(
                        "role", msg.getRole(),
                        "content", msg.getContent()
                ))
                .toList();
    }

    /**
     * Stänger en session.
     */
    public void closeSession(String sessionId) {
        sessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            session.close();
            sessionRepository.save(session);
            log.info("Closed coach session {}", sessionId);
        });
    }
}

