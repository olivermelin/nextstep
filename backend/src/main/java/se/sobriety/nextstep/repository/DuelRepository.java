package se.sobriety.nextstep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.sobriety.nextstep.entity.Duel;

import java.util.List;

public interface DuelRepository extends JpaRepository<Duel, Long> {

    /** Hämta alla dueller som involverar en användare */
    @Query("SELECT d FROM Duel d WHERE d.challengerId = :userId OR d.challengedId = :userId ORDER BY d.createdAt DESC")
    List<Duel> findAllByUserId(@Param("userId") String userId);

    /** Radera alla dueller för en användare (GDPR) */
    @Query("DELETE FROM Duel d WHERE d.challengerId = :userId OR d.challengedId = :userId")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteAllByUserId(@Param("userId") String userId);
}
