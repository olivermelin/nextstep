package se.sobriety.nextstep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sobriety.nextstep.entity.Collectible;

import java.util.List;
import java.util.Optional;

public interface CollectibleRepository extends JpaRepository<Collectible, Long> {

    List<Collectible> findByUserId(String userId);

    Optional<Collectible> findByUserIdAndCollectibleKey(String userId, String key);
}
