package se.sobriety.nextstep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sobriety.nextstep.entity.ActivityType;
import se.sobriety.nextstep.entity.DailyActivity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyActivityRepository extends JpaRepository<DailyActivity, Long> {

    List<DailyActivity> findByUserId(String userId);

    List<DailyActivity> findByUserIdAndActivityDateBetween(String userId, LocalDate from, LocalDate to);

    Optional<DailyActivity> findByUserIdAndActivityDateAndActivityType(
            String userId, LocalDate date, ActivityType type);

    List<DailyActivity> findByUserIdAndActivityDate(String userId, LocalDate date);
}
