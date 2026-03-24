package se.sobriety.nextstep.entity;

public enum DuelStatus {
    PENDING,    // Väntar på att motståndaren accepterar
    ACTIVE,     // Båda har accepterat, utmaningen pågår
    COMPLETED,  // Någon har vunnit
    DECLINED,   // Motståndaren avböjde
    EXPIRED     // Ingen svarade inom 48h
}
