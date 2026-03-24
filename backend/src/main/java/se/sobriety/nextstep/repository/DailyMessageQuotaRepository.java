package se.sobriety.nextstep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sobriety.nextstep.entity.DailyMessageQuota;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyMessageQuotaRepository extends JpaRepository<DailyMessageQuota, Long> {
    Optional<DailyMessageQuota> findByUserIdAndQuotaDate(String userId, LocalDate quotaDate);
    void deleteByUserId(String userId);
}
