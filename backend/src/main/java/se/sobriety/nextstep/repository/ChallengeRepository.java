package se.sobriety.nextstep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sobriety.nextstep.entity.Challenge;
import se.sobriety.nextstep.entity.ChallengeCategory;
import se.sobriety.nextstep.entity.ChallengeDifficulty;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    List<Challenge> findByCategory(ChallengeCategory category);

    List<Challenge> findByDifficulty(ChallengeDifficulty difficulty);

    List<Challenge> findByCategoryAndDifficulty(ChallengeCategory category, ChallengeDifficulty difficulty);
}

