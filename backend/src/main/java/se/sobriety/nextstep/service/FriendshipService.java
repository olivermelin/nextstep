package se.sobriety.nextstep.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.dto.FriendDto;
import se.sobriety.nextstep.entity.Friendship;
import se.sobriety.nextstep.entity.FriendshipStatus;
import se.sobriety.nextstep.repository.FriendshipRepository;
import se.sobriety.nextstep.repository.UserProgressRepository;
import se.sobriety.nextstep.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final UserProgressRepository progressRepository;

    /**
     * Skicka en vänförfrågan till en annan användare (via e-post).
     */
    @Transactional
    public FriendDto sendRequest(String requesterId, String targetEmail) {
        if (requesterId.equals(targetEmail)) {
            throw new IllegalArgumentException("Du kan inte lägga till dig själv");
        }

        // Kolla att målanvändaren existerar
        var target = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new IllegalArgumentException("Ingen användare med den e-postadressen hittades"));

        // Kolla om relation redan finns
        friendshipRepository.findBetween(requesterId, targetEmail).ifPresent(existing -> {
            throw new IllegalStateException("Vänförfrågan finns redan");
        });

        var requesterName = userRepository.findByEmail(requesterId)
                .map(u -> u.getName() != null ? u.getName() : "Okänd")
                .orElse("Okänd");

        var friendship = new Friendship(requesterId, requesterName, targetEmail);
        friendship = friendshipRepository.save(friendship);

        var targetPoints = progressRepository.findByUserId(targetEmail)
                .map(p -> p.getTotalPoints()).orElse(0);

        return toDto(friendship, requesterId, target.getName(), targetPoints);
    }

    /**
     * Acceptera eller avvisa en inkommande vänförfrågan.
     */
    @Transactional
    public FriendDto respondToRequest(Long friendshipId, String addresseeId, boolean accept) {
        var friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Vänförfrågan hittades inte"));

        if (!friendship.getAddresseeId().equals(addresseeId)) {
            throw new SecurityException("Du kan bara svara på dina egna förfrågningar");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("Denna förfrågan är redan besvarad");
        }

        friendship.setStatus(accept ? FriendshipStatus.ACCEPTED : FriendshipStatus.DECLINED);
        friendship = friendshipRepository.save(friendship);

        var requesterPoints = progressRepository.findByUserId(friendship.getRequesterId())
                .map(p -> p.getTotalPoints()).orElse(0);

        return toDto(friendship, addresseeId, friendship.getRequesterName(), requesterPoints);
    }

    /**
     * Hämta alla accepterade vänner för en användare.
     */
    @Transactional(readOnly = true)
    public List<FriendDto> getFriends(String userId) {
        var friendships = friendshipRepository.findAcceptedFriendships(userId);
        var result = new ArrayList<FriendDto>();

        for (var f : friendships) {
            var friendId = f.getRequesterId().equals(userId) ? f.getAddresseeId() : f.getRequesterId();
            var friendName = resolveName(f, userId);
            var friendPoints = progressRepository.findByUserId(friendId)
                    .map(p -> p.getTotalPoints()).orElse(0);
            result.add(toDto(f, userId, friendName, friendPoints));
        }

        return result;
    }

    /**
     * Hämta inkommande väntande vänförfrågningar.
     */
    @Transactional(readOnly = true)
    public List<FriendDto> getPendingRequests(String userId) {
        var incoming = friendshipRepository.findByAddresseeIdAndStatus(userId, FriendshipStatus.PENDING);
        var result = new ArrayList<FriendDto>();

        for (var f : incoming) {
            var requesterPoints = progressRepository.findByUserId(f.getRequesterId())
                    .map(p -> p.getTotalPoints()).orElse(0);
            result.add(toDto(f, userId, f.getRequesterName(), requesterPoints));
        }

        return result;
    }

    /**
     * Ta bort en vänrelation.
     */
    @Transactional
    public void removeFriend(Long friendshipId, String userId) {
        var friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Vänrelation hittades inte"));

        if (!friendship.getRequesterId().equals(userId) && !friendship.getAddresseeId().equals(userId)) {
            throw new SecurityException("Åtkomst nekad");
        }

        friendshipRepository.delete(friendship);
    }

    /** Radera alla vänrelationer för en användare (GDPR) */
    @Transactional
    public void deleteAllForUser(String userId) {
        friendshipRepository.deleteAllByUserId(userId);
    }

    // --- Hjälpmetoder ---

    private String resolveName(Friendship f, String currentUserId) {
        if (f.getRequesterId().equals(currentUserId)) {
            // Jag skickade, motparten är addressee
            return userRepository.findByEmail(f.getAddresseeId())
                    .map(u -> u.getName() != null ? u.getName() : "Okänd")
                    .orElse("Okänd");
        } else {
            // Jag mottog, motparten är requester
            return f.getRequesterName();
        }
    }

    private FriendDto toDto(Friendship f, String currentUserId, String otherName, int otherPoints) {
        boolean iRequested = f.getRequesterId().equals(currentUserId);
        String since = f.getCreatedAt() != null ? f.getCreatedAt().toLocalDate().toString() : null;

        return new FriendDto(
                f.getId(),
                otherName,
                otherPoints,
                f.getStatus().name(),
                iRequested,
                since
        );
    }
}
