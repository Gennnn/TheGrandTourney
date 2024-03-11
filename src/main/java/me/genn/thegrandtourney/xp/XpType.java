package me.genn.thegrandtourney.xp;

public enum XpType {
    COMBAT, FISHING, FARMING, MINING, LOGGING, BLACKSMITHING, TAILORING, COOKING, TINKERING;

    public String getName() {
        switch (this) {
            case TINKERING -> {
                return "Tinkering";
            } case BLACKSMITHING -> {
                return "Blacksmithing";
            } case TAILORING -> {
                return "Tailoring";
            } case COOKING -> {
                return "Cooking";
            }case FARMING -> {
                return "Farming";
            }case FISHING -> {
                return "Fishing";
            }case MINING -> {
                return "Mining";
            }case COMBAT -> {
                return "Combat";
            } case LOGGING -> {
                return "Logging";
            }
        }
        return "";
    }
}
