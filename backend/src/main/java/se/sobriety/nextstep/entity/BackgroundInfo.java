package se.sobriety.nextstep.entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Bakgrundsinformation om användaren - embedded i UserSettings
 */
@Embeddable
@Getter
@Setter
public class BackgroundInfo {

    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private CurrentSituation currentSituation;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<SubstanceType> substanceHistory = new ArrayList<>();

    public BackgroundInfo() {
    }

    public BackgroundInfo(Integer age, Gender gender, CurrentSituation currentSituation, List<SubstanceType> substanceHistory) {
        this.age = age;
        this.gender = gender;
        this.currentSituation = currentSituation;
        this.substanceHistory = substanceHistory != null ? substanceHistory : new ArrayList<>();
    }
}

