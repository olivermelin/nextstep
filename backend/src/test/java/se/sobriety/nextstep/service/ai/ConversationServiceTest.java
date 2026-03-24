package se.sobriety.nextstep.service.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import se.sobriety.nextstep.entity.*;
import se.sobriety.nextstep.repository.CoachMessageRepository;
import se.sobriety.nextstep.repository.CoachSessionRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConversationServiceTest {

    @Mock
    private CoachSessionRepository sessionRepository;
    @Mock
    private CoachMessageRepository messageRepository;

    private ConversationService conversationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        conversationService = new ConversationService(sessionRepository, messageRepository);
    }

    // ---- getOrCreateActiveSession ----

    @Test
    void getOrCreateActiveSession_existingSession_returnsIt() {
        CoachSession existing = new CoachSession("user-1");
        when(sessionRepository.findByUserIdAndStatus("user-1", SessionStatus.ACTIVE))
                .thenReturn(Optional.of(existing));

        CoachSession result = conversationService.getOrCreateActiveSession("user-1");

        assertSame(existing, result);
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void getOrCreateActiveSession_noSession_createsNew() {
        when(sessionRepository.findByUserIdAndStatus("user-2", SessionStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(sessionRepository.save(any(CoachSession.class))).thenAnswer(inv -> inv.getArgument(0));

        CoachSession result = conversationService.getOrCreateActiveSession("user-2");

        assertNotNull(result);
        assertEquals("user-2", result.getUserId());
        assertTrue(result.isActive());
        verify(sessionRepository).save(any(CoachSession.class));
    }

    // ---- getSession ----

    @Test
    void getSession_found_returnsOptional() {
        CoachSession session = new CoachSession("user-1");
        when(sessionRepository.findBySessionId("sess-123")).thenReturn(Optional.of(session));

        Optional<CoachSession> result = conversationService.getSession("sess-123");

        assertTrue(result.isPresent());
        assertEquals("user-1", result.get().getUserId());
    }

    @Test
    void getSession_notFound_returnsEmpty() {
        when(sessionRepository.findBySessionId("nonexistent")).thenReturn(Optional.empty());

        Optional<CoachSession> result = conversationService.getSession("nonexistent");

        assertTrue(result.isEmpty());
    }

    // ---- saveMessage ----

    @Test
    void saveMessage_savesAndTouchesSession() {
        CoachSession session = new CoachSession("user-1");
        when(messageRepository.save(any(CoachMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        CoachMessage result = conversationService.saveMessage(session, "user", "Hello");

        assertEquals("user", result.getRole());
        assertEquals("Hello", result.getContent());
        assertFalse(result.isCrisisDetected());
        verify(sessionRepository).save(session);
        verify(messageRepository).save(any(CoachMessage.class));
    }

    // ---- saveMessageWithCrisis ----

    @Test
    void saveMessageWithCrisis_flagsCrisis() {
        CoachSession session = new CoachSession("user-1");
        when(messageRepository.save(any(CoachMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        CoachMessage result = conversationService.saveMessageWithCrisis(
                session, "user", "I need help", CrisisLevel.CRITICAL);

        assertTrue(result.isCrisisDetected());
        assertEquals(CrisisLevel.CRITICAL, result.getCrisisLevel());
        verify(sessionRepository).save(session);
    }

    // ---- buildMessageHistory ----

    @Test
    void buildMessageHistory_noSession_returnsEmptyList() {
        when(sessionRepository.findBySessionId("nonexistent")).thenReturn(Optional.empty());

        List<Map<String, String>> history = conversationService.buildMessageHistory("nonexistent");

        assertTrue(history.isEmpty());
    }

    @Test
    void buildMessageHistory_withMessages_returnsChronological() {
        CoachSession session = new CoachSession("user-1");
        session.setId(1L);
        when(sessionRepository.findBySessionId(session.getSessionId()))
                .thenReturn(Optional.of(session));

        // Simulate desc order from DB (newest first)
        CoachMessage msg2 = new CoachMessage(session, "assistant", "Hi there");
        CoachMessage msg1 = new CoachMessage(session, "user", "Hello");
        when(messageRepository.findBySessionIdOrderByTimestampDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of(msg2, msg1));

        List<Map<String, String>> history = conversationService.buildMessageHistory(session.getSessionId());

        assertEquals(2, history.size());
        // Should be reversed to chronological
        assertEquals("user", history.get(0).get("role"));
        assertEquals("Hello", history.get(0).get("content"));
        assertEquals("assistant", history.get(1).get("role"));
    }

    // ---- closeSession ----

    @Test
    void closeSession_existingSession_closesIt() {
        CoachSession session = new CoachSession("user-1");
        assertTrue(session.isActive());
        when(sessionRepository.findBySessionId(session.getSessionId()))
                .thenReturn(Optional.of(session));

        conversationService.closeSession(session.getSessionId());

        assertFalse(session.isActive());
        verify(sessionRepository).save(session);
    }

    @Test
    void closeSession_nonexistentSession_doesNothing() {
        when(sessionRepository.findBySessionId("nonexistent")).thenReturn(Optional.empty());

        conversationService.closeSession("nonexistent");

        verify(sessionRepository, never()).save(any());
    }
}
