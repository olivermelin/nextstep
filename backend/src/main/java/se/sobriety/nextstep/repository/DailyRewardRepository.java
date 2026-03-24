package se.sobriety.nextstep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sobriety.nextstep.entity.DailyReward;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyRewardRepository extends JpaRepository<DailyReward, Long> {

    Optional<DailyReward> findByUserIdAndRewardDate(String userId, LocalDate date);
}
