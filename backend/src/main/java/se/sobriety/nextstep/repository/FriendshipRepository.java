package se.sobriety.nextstep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.sobriety.nextstep.entity.Friendship;
import se.sobriety.nextstep.entity.FriendshipStatus;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /** Hämta alla vänner (accepterade relationer) för en användare */
    @Query("SELECT f FROM Friendship f WHERE (f.requesterId = :userId OR f.addresseeId = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendships(@Param("userId") String userId);

    /** Hämta inkommande förfrågningar (pending, där userId är addressee) */
    List<Friendship> findByAddresseeIdAndStatus(String addresseeId, FriendshipStatus status);

    /** Hämta utgående förfrågningar (pending, där userId är requester) */
    List<Friendship> findByRequesterIdAndStatus(String requesterId, FriendshipStatus status);

    /** Kolla om relation redan existerar (åt vilket håll som helst) */
    @Query("SELECT f FROM Friendship f WHERE (f.requesterId = :a AND f.addresseeId = :b) OR (f.requesterId = :b AND f.addresseeId = :a)")
    Optional<Friendship> findBetween(@Param("a") String a, @Param("b") String b);

    /** Radera alla relationer för en användare (GDPR) */
    @Query("DELETE FROM Friendship f WHERE f.requesterId = :userId OR f.addresseeId = :userId")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteAllByUserId(@Param("userId") String userId);
}
