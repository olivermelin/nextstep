package se.sobriety.nextstep.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Ett enskilt meddelande i en coach-konversation.
 */
@Entity
@Table(name = "coach_messages")
@Getter
@Setter
public class CoachMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private CoachSession session;

    @Column(nullable = false)
    private String role; // "user" eller "assistant"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean crisisDetected;

    @Enumerated(EnumType.STRING)
    private CrisisLevel crisisLevel;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    protected CoachMessage() {
    }

    public CoachMessage(CoachSession session, String role, String content) {
        this.session = session;
        this.role = role;
        this.content = content;
        this.crisisDetected = false;
        this.crisisLevel = CrisisLevel.NONE;
        this.timestamp = LocalDateTime.now();
    }

    public void flagCrisis(CrisisLevel level) {
        this.crisisDetected = (level == CrisisLevel.CRITICAL || level == CrisisLevel.ELEVATED);
        this.crisisLevel = level;
    }
}

