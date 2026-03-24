package se.sobriety.nextstep.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representation av vänskapsrelation mellan två användare.
 * requesterId skickar förfrågan, addresseeId tar emot.
 */
@Entity
@Getter
@Setter
@Table(
    name = "friendships",
    indexes = {
        @Index(name = "idx_friendship_requester", columnList = "requesterId"),
        @Index(name = "idx_friendship_addressee", columnList = "addresseeId")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_friendship_pair", columnNames = {"requesterId", "addresseeId"})
    }
)
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String requesterId;

    @Column(nullable = false, updatable = false)
    private String addresseeId;

    /** Visningsnamn för den som skickar förfrågan (cachat vid skapande) */
    @Column(nullable = false)
    private String requesterName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    protected Friendship() {
    }

    public Friendship(String requesterId, String requesterName, String addresseeId) {
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.addresseeId = addresseeId;
        this.status = FriendshipStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
