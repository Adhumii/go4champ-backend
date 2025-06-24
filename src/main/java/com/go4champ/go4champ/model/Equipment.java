package com.go4champ.go4champ.model;

public enum Equipment {
    JUMP_ROPE("Jump Rope"),
    PULL_UP_BAR("Pull-Up Bar"),
    KETTLEBELL("Kettlebell"),
    RESISTANCE_BAND("Resistance Band"),
    DUMBBELLS("Dumbbells"),
    MAT("Mat");

    private final String displayName;

    Equipment(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Hilfsmethode um String zu Equipment zu konvertieren
    public static Equipment fromString(String equipment) {
        for (Equipment e : Equipment.values()) {
            if (e.name().equalsIgnoreCase(equipment) ||
                    e.getDisplayName().equalsIgnoreCase(equipment)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown equipment: " + equipment);
    }

    // Alle verfügbaren Equipment-Namen als String-Array
    public static String[] getAllNames() {
        Equipment[] values = Equipment.values();
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].name();
        }
        return names;
    }

    // Alle Display-Namen für Frontend
    public static String[] getAllDisplayNames() {
        Equipment[] values = Equipment.values();
        String[] displayNames = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            displayNames[i] = values[i].getDisplayName();
        }
        return displayNames;
    }
}