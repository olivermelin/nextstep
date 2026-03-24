package se.sobriety.nextstep.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 1v1-duell mellan två användare kring en specifik utmaning.
 * Challenger utmanar challenged. Båda ska genomföra utmaningen —
 * den som markerar klart först vinner.
 */
@Entity
@Getter
@Setter
@Table(
    name = "duels",
    indexes = {
        @Index(name = "idx_duel_challenger", columnList = "challengerId"),
        @Index(name = "idx_duel_challenged", columnList = "challengedId")
    }
)
public class Duel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String challengerId;

    @Column(nullable = false, updatable = false)
    private String challengedId;

    /** Visningsnamn (cachade vid skapande för att undvika extra joins) */
    @Column(nullable = false)
    private String challengerName;

    @Column(nullable = false)
    private String challengedName;

    @Column(nullable = false)
    private Long challengeId;

    @Column(nullable = false)
    private String challengeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DuelStatus status = DuelStatus.PENDING;

    /** Null tills duellen är avgjord */
    private String winnerId;

    private LocalDateTime challengerCompletedAt;
    private LocalDateTime challengedCompletedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Duellen förfaller efter 48h om den inte accepteras */
    private LocalDateTime expiresAt;

    private LocalDateTime completedAt;

    protected Duel() {
    }

    public Duel(String challengerId, String challengerName,
                String challengedId, String challengedName,
                Long challengeId, String challengeName) {
        this.challengerId = challengerId;
        this.challengerName = challengerName;
        this.challengedId = challengedId;
        this.challengedName = challengedName;
        this.challengeId = challengeId;
        this.challengeName = challengeName;
        this.status = DuelStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(48);
    }
}
