package se.sobriety.nextstep.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.dto.CreateDuelDto;
import se.sobriety.nextstep.dto.DuelDto;
import se.sobriety.nextstep.entity.Duel;
import se.sobriety.nextstep.entity.DuelStatus;
import se.sobriety.nextstep.repository.DuelRepository;
import se.sobriety.nextstep.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuelService {

    private final DuelRepository duelRepository;
    private final UserRepository userRepository;

    /**
     * Skapa en ny duell — utmanaren utmanar en annan användare.
     */
    @Transactional
    public DuelDto createDuel(String challengerId, CreateDuelDto request) {
        if (challengerId.equals(request.challengedEmail())) {
            throw new IllegalArgumentException("Du kan inte utmana dig själv");
        }

        var challenged = userRepository.findByEmail(request.challengedEmail())
                .orElseThrow(() -> new IllegalArgumentException("Ingen användare med den e-postadressen hittades"));

        var challenger = userRepository.findByEmail(challengerId)
                .orElseThrow(() -> new IllegalStateException("Inloggad användare hittades inte"));

        var challengerName = challenger.getName() != null ? challenger.getName() : "Okänd";
        var challengedName = challenged.getName() != null ? challenged.getName() : "Okänd";

        var duel = new Duel(challengerId, challengerName,
                request.challengedEmail(), challengedName,
                request.challengeId(), request.challengeName());

        duel = duelRepository.save(duel);
        return toDto(duel, challengerId);
    }

    /**
     * Acceptera eller avvisa en duell.
     */
    @Transactional
    public DuelDto respondToDuel(Long duelId, String challengedId, boolean accept) {
        var duel = duelRepository.findById(duelId)
                .orElseThrow(() -> new IllegalArgumentException("Duellen hittades inte"));

        if (!duel.getChallengedId().equals(challengedId)) {
            throw new SecurityException("Åtkomst nekad");
        }

        if (duel.getStatus() != DuelStatus.PENDING) {
            throw new IllegalStateException("Duellen är inte längre väntande");
        }

        if (duel.getExpiresAt().isBefore(LocalDateTime.now())) {
            duel.setStatus(DuelStatus.EXPIRED);
            duelRepository.save(duel);
            throw new IllegalStateException("Duellen har gått ut");
        }

        duel.setStatus(accept ? DuelStatus.ACTIVE : DuelStatus.DECLINED);
        duel = duelRepository.save(duel);
        return toDto(duel, challengedId);
    }

    /**
     * Markera att en deltagare har slutfört sin del av duellen.
     * Den som slutför först vinner.
     */
    @Transactional
    public DuelDto completeDuel(Long duelId, String userId) {
        var duel = duelRepository.findById(duelId)
                .orElseThrow(() -> new IllegalArgumentException("Duellen hittades inte"));

        if (duel.getStatus() != DuelStatus.ACTIVE) {
            throw new IllegalStateException("Duellen är inte aktiv");
        }

        boolean isChallenger = duel.getChallengerId().equals(userId);
        boolean isChallenged = duel.getChallengedId().equals(userId);

        if (!isChallenger && !isChallenged) {
            throw new SecurityException("Åtkomst nekad");
        }

        LocalDateTime now = LocalDateTime.now();

        if (isChallenger && duel.getChallengerCompletedAt() == null) {
            duel.setChallengerCompletedAt(now);
        } else if (isChallenged && duel.getChallengedCompletedAt() == null) {
            duel.setChallengedCompletedAt(now);
        } else {
            throw new IllegalStateException("Du har redan markerat din del som klar");
        }

        // Avgör vinnare om båda klara, eller sätt status till COMPLETED direkt för den första
        if (duel.getChallengerCompletedAt() != null && duel.getChallengedCompletedAt() != null) {
            // Jämför vem som slutförde först
            boolean challengerFirst = duel.getChallengerCompletedAt()
                    .isBefore(duel.getChallengedCompletedAt());
            duel.setWinnerId(challengerFirst ? duel.getChallengerId() : duel.getChallengedId());
            duel.setStatus(DuelStatus.COMPLETED);
            duel.setCompletedAt(now);
        } else if (duel.getChallengerCompletedAt() != null || duel.getChallengedCompletedAt() != null) {
            // Första deltagaren klar — markeras direkt som vinnare om motparten inte svarar inom 7 dagar
            // För nu: håll ACTIVE tills båda slutfört
        }

        duel = duelRepository.save(duel);
        return toDto(duel, userId);
    }

    /**
     * Hämta alla dueller för en användare.
     */
    @Transactional(readOnly = true)
    public List<DuelDto> getDuels(String userId) {
        return duelRepository.findAllByUserId(userId).stream()
                .map(d -> toDto(d, userId))
                .toList();
    }

    /** Radera alla dueller för en användare (GDPR) */
    @Transactional
    public void deleteAllForUser(String userId) {
        duelRepository.deleteAllByUserId(userId);
    }

    // --- Hjälpmetod ---

    private DuelDto toDto(Duel d, String currentUserId) {
        boolean iAmChallenger = d.getChallengerId().equals(currentUserId);
        boolean iCompleted = iAmChallenger
                ? d.getChallengerCompletedAt() != null
                : d.getChallengedCompletedAt() != null;
        boolean opponentCompleted = iAmChallenger
                ? d.getChallengedCompletedAt() != null
                : d.getChallengerCompletedAt() != null;

        String winnerName = null;
        if (d.getWinnerId() != null) {
            winnerName = d.getWinnerId().equals(d.getChallengerId())
                    ? d.getChallengerName()
                    : d.getChallengedName();
        }

        return new DuelDto(
                d.getId(),
                d.getChallengerName(),
                d.getChallengedName(),
                d.getChallengeId(),
                d.getChallengeName(),
                d.getStatus().name(),
                d.getWinnerId(),
                winnerName,
                iAmChallenger,
                iCompleted,
                opponentCompleted,
                d.getCreatedAt() != null ? d.getCreatedAt().toString() : null,
                d.getExpiresAt() != null ? d.getExpiresAt().toString() : null,
                d.getCompletedAt() != null ? d.getCompletedAt().toString() : null
        );
    }
}
