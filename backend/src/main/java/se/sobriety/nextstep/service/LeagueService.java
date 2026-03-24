package se.sobriety.nextstep.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.dto.LeagueEntryDto;
import se.sobriety.nextstep.entity.LeagueTier;
import se.sobriety.nextstep.repository.UserProgressRepository;
import se.sobriety.nextstep.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeagueService {

    private final UserProgressRepository progressRepository;
    private final UserRepository userRepository;

    private static final int TOP_SIZE = 50;

    /**
     * Hämta ligalistan (top 50 användare sorterade efter totalPoints).
     * Tier tilldelas dynamiskt: DIAMOND (top 5), GOLD (top 25%), SILVER (top 50%), BRONZE (resten).
     *
     * @param requestingUserId inloggad användares userId (för att markera "isMe")
     */
    @Transactional(readOnly = true)
    public List<LeagueEntryDto> getLeaderboard(String requestingUserId) {
        var topProgress = progressRepository.findAllByOrderByTotalPointsDesc(
                PageRequest.of(0, TOP_SIZE));

        if (topProgress.isEmpty()) {
            return List.of();
        }

        // Hämta alla berörda e-poster för namn-lookup
        var userIds = topProgress.stream()
                .map(p -> p.getUserId())
                .collect(Collectors.toList());

        // Batch-hämta namn (en query per userId — acceptabelt för 50 poster)
        Map<String, String> nameMap = userIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> userRepository.findByEmail(id)
                                .map(u -> u.getName() != null ? u.getName() : maskEmail(id))
                                .orElse(maskEmail(id))
                ));

        int total = topProgress.size();
        var result = new ArrayList<LeagueEntryDto>(total);

        for (int i = 0; i < total; i++) {
            var progress = topProgress.get(i);
            int rank = i + 1;
            LeagueTier tier = assignTier(rank, total);
            String displayName = nameMap.getOrDefault(progress.getUserId(), "Okänd");
            boolean isMe = progress.getUserId().equals(requestingUserId);

            result.add(new LeagueEntryDto(
                    rank,
                    displayName,
                    progress.getTotalPoints(),
                    tier.name(),
                    isMe
            ));
        }

        return result;
    }

    /**
     * Hämta en enskild användares ligaposition.
     */
    @Transactional(readOnly = true)
    public LeagueEntryDto getUserLeagueEntry(String userId) {
        var allProgress = progressRepository.findAllByOrderByTotalPointsDesc(
                PageRequest.of(0, Integer.MAX_VALUE));

        int total = allProgress.size();
        for (int i = 0; i < total; i++) {
            var p = allProgress.get(i);
            if (p.getUserId().equals(userId)) {
                int rank = i + 1;
                String displayName = userRepository.findByEmail(userId)
                        .map(u -> u.getName() != null ? u.getName() : maskEmail(userId))
                        .orElse(maskEmail(userId));
                return new LeagueEntryDto(rank, displayName, p.getTotalPoints(),
                        assignTier(rank, total).name(), true);
            }
        }

        // Användaren har inga poäng ännu
        return new LeagueEntryDto(total + 1, userId, 0, LeagueTier.BRONZE.name(), true);
    }

    // --- Tier-beräkning ---

    private LeagueTier assignTier(int rank, int total) {
        if (rank <= Math.max(1, total / 20)) return LeagueTier.DIAMOND; // top 5%
        if (rank <= Math.max(2, total / 4))  return LeagueTier.GOLD;    // top 25%
        if (rank <= Math.max(4, total / 2))  return LeagueTier.SILVER;  // top 50%
        return LeagueTier.BRONZE;
    }

    /** Dölj e-post för privacy — visar bara första delen */
    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return "Användare";
        return email.substring(0, Math.min(at, 8)) + "...";
    }
}
