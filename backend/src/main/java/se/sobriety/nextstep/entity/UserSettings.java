package se.sobriety.nextstep.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(indexes = @Index(name = "idx_user_settings_user_id", columnList = "userId"))
public class UserSettings extends BaseUserData {

    private String name;
    private String email;
    private String phone;
    private String language;
    private boolean notificationsEnabled;
    private boolean aiNotificationsEnabled;
    private boolean darkModeEnabled;

    // Onboarding-relaterade fält
    private Boolean onboardingCompleted = false;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<UserGoal> userGoals = new ArrayList<>();

    private String otherGoal;

    @Enumerated(EnumType.STRING)
    private RecoveryStage recoveryStage;

    @Embedded
    private BackgroundInfo backgroundInfo;

    protected UserSettings() {}

    public UserSettings(String userId) {
        this.userId = userId;
        this.name = "Default Name";
        this.email = "";
        this.phone = "";
        this.language = "en";
        this.notificationsEnabled = true;
        this.aiNotificationsEnabled = false;
        this.darkModeEnabled = false;
        this.onboardingCompleted = false;
        this.lastUpdated = LocalDateTime.now();
    }
}
