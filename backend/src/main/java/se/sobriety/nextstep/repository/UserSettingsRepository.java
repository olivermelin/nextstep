package se.sobriety.nextstep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sobriety.nextstep.entity.UserSettings;

import java.util.List;
import java.util.Optional;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {

    Optional<UserSettings> findByUserId(String userId);

    List<UserSettings> findAllByUserId(String userId);

    List<UserSettings> findAllByEmail(String email);
}
