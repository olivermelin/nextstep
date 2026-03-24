package se.sobriety.nextstep.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.dto.CoachMessageDto;
import se.sobriety.nextstep.dto.CoachSessionMessagesDto;
import se.sobriety.nextstep.dto.CoachSessionSummaryDto;
import se.sobriety.nextstep.entity.*;
import se.sobriety.nextstep.repository.CoachMessageRepository;
import se.sobriety.nextstep.repository.CoachSessionRepository;

import java.util.*;
import java.util.stream.Collectors;

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

    // ── Multi-konversation ────────────────────────────────────────────

    /**
     * Hämta alla sessioner för en användare, sorterade nyast först.
     * Returnerar sammanfattningar med preview (första user-meddelandet).
     */
    @Transactional(readOnly = true)
    public List<CoachSessionSummaryDto> getUserSessions(String userId) {
        List<CoachSession> sessions = sessionRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return sessions.stream().map(session -> {
            // Hämta första user-meddelandet som preview
            String preview = session.getMessages().stream()
                    .filter(m -> "user".equals(m.getRole()))
                    .map(CoachMessage::getContent)
                    .findFirst()
                    .map(content -> content.length() > 80 ? content.substring(0, 80) + "..." : content)
                    .orElse("");

            int messageCount = session.getMessages().size();

            return new CoachSessionSummaryDto(
                    session.getSessionId(),
                    session.getStatus().name(),
                    preview,
                    session.getCreatedAt().toString(),
                    session.getLastMessageAt().toString(),
                    messageCount
            );
        }).collect(Collectors.toList());
    }

    /**
     * Hämta alla meddelanden för en specifik session. Verifierar att sessionen tillhör userId.
     */
    @Transactional(readOnly = true)
    public CoachSessionMessagesDto getSessionMessages(String sessionId, String userId) {
        CoachSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (!session.getUserId().equals(userId)) {
            throw new SecurityException("Access denied to session " + sessionId);
        }

        List<CoachMessageDto> messages = messageRepository
                .findBySession_SessionIdOrderByTimestampAsc(sessionId)
                .stream()
                .map(msg -> new CoachMessageDto(
                        msg.getRole(),
                        msg.getContent(),
                        msg.getCrisisLevel() != null ? msg.getCrisisLevel().name() : "NONE",
                        msg.getTimestamp().toString()
                ))
                .collect(Collectors.toList());

        return new CoachSessionMessagesDto(
                session.getSessionId(),
                session.getStatus().name(),
                session.getCreatedAt().toString(),
                messages
        );
    }

    /**
     * Hämta eller återaktivera en session. Om sessionId anges, återaktivera den sessionen
     * (stäng andra aktiva först). Annars, delegera till getOrCreateActiveSession.
     */
    public CoachSession getOrReactivateSession(String userId, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return getOrCreateActiveSession(userId);
        }

        CoachSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (!session.getUserId().equals(userId)) {
            throw new SecurityException("Access denied to session " + sessionId);
        }

        if (!session.isActive()) {
            // Stäng ev. annan aktiv session först
            sessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE)
                    .ifPresent(active -> {
                        active.close();
                        sessionRepository.save(active);
                        log.info("Closed previous active session {} to reactivate {}", active.getSessionId(), sessionId);
                    });

            session.reactivate();
            sessionRepository.save(session);
            log.info("Reactivated session {} for user {}", sessionId, userId);
        }

        return session;
    }

    /**
     * Tar bort en session och alla dess meddelanden permanent.
     * Verifierar att sessionen tillhör userId.
     */
    public void deleteSession(String sessionId, String userId) {
        CoachSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (!session.getUserId().equals(userId)) {
            throw new SecurityException("Access denied to session " + sessionId);
        }

        sessionRepository.delete(session);
        log.info("Deleted session {} for user {}", sessionId, userId);
    }

    /**
     * Skapa en ny session. Stänger ev. aktiv session först.
     */
    public CoachSession createNewSession(String userId) {
        // Stäng aktiv session om det finns en
        sessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE)
                .ifPresent(active -> {
                    active.close();
                    sessionRepository.save(active);
                    log.info("Closed active session {} before creating new for user {}", active.getSessionId(), userId);
                });

        CoachSession newSession = new CoachSession(userId);
        sessionRepository.save(newSession);
        log.info("Created new session {} for user {}", newSession.getSessionId(), userId);
        return newSession;
    }
}

