package se.sobriety.nextstep.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sobriety.nextstep.entity.UserProgress;

import java.util.List;
import java.util.Optional;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    Optional<UserProgress> findByUserId(String userId);

    void deleteByUserId(String userId);

    /** Hämta topplista sorterad efter totalPoints (för liga) */
    List<UserProgress> findAllByOrderByTotalPointsDesc(Pageable pageable);
}

