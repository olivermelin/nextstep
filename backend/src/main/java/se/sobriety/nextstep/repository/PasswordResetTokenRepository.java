package se.sobriety.nextstep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sobriety.nextstep.entity.PasswordResetToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByExpiresAtBefore(LocalDateTime cutoff);
}
