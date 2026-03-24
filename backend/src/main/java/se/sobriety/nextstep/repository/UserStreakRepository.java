package se.sobriety.nextstep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sobriety.nextstep.entity.UserStreak;

import java.util.Optional;

public interface UserStreakRepository extends JpaRepository<UserStreak, Long> {
    Optional<UserStreak> findByUserId(String userId);
    void deleteByUserId(String userId);
}
