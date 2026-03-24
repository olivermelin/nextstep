package se.sobriety.nextstep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sobriety.nextstep.entity.UserAchievement;

import java.util.List;
import java.util.Optional;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    List<UserAchievement> findByUserId(String userId);

    Optional<UserAchievement> findByUserIdAndAchievementId(String userId, Long achievementId);

    void deleteByUserId(String userId);
}

