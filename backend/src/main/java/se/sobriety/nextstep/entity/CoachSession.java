package se.sobriety.nextstep.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representerar en coach-session med en användare.
 * En användare kan ha en aktiv session åt gången.
 */
@Entity
@Table(name = "coach_sessions")
@Getter
@Setter
public class CoachSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sessionId;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastMessageAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("timestamp ASC")
    private List<CoachMessage> messages = new ArrayList<>();

    protected CoachSession() {
    }

    public CoachSession(String userId) {
        this.sessionId = UUID.randomUUID().toString();
        this.userId = userId;
        this.status = SessionStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.lastMessageAt = LocalDateTime.now();
    }

    public void touch() {
        this.lastMessageAt = LocalDateTime.now();
    }

    public void close() {
        this.status = SessionStatus.CLOSED;
    }

    public boolean isActive() {
        return this.status == SessionStatus.ACTIVE;
    }
}

