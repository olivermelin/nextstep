package se.sobriety.nextstep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sobriety.nextstep.entity.CategoryProgress;
import se.sobriety.nextstep.entity.ChallengeCategory;

import java.util.List;
import java.util.Optional;

public interface CategoryProgressRepository extends JpaRepository<CategoryProgress, Long> {

    List<CategoryProgress> findByUserId(String userId);

    Optional<CategoryProgress> findByUserIdAndCategory(String userId, ChallengeCategory category);

    void deleteByUserId(String userId);
}

