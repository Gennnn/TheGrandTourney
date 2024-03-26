package me.genn.thegrandtourney.xp;

public enum XpType {
    COMBAT, FISHING, FARMING, MINING, LOGGING, BLACKSMITHING, TAILORING, COOKING, TINKERING, ALCHEMY, CARPENTRY, ALL;

    public String getName() {
        switch (this) {
            case TINKERING -> {
                return "Tinkering";
            } case BLACKSMITHING -> {
                return "Smithing";
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
                return "Foraging";
            } case CARPENTRY -> {
                return "Carpentry";
            } case ALCHEMY -> {
                return "Alchemy";
            }
        }
        return "";
    }
}
