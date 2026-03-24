package se.sobriety.nextstep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sobriety.nextstep.entity.DailyCheckIn;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyCheckInRepository extends JpaRepository<DailyCheckIn, Long> {

    Optional<DailyCheckIn> findByUserIdAndCheckInDate(String userId, LocalDate date);

    List<DailyCheckIn> findByUserIdOrderByCheckInDateDesc(String userId);

    List<DailyCheckIn> findByUserIdAndCheckInDateBetweenOrderByCheckInDateDesc(
            String userId, LocalDate from, LocalDate to);

    void deleteByUserId(String userId);
}
