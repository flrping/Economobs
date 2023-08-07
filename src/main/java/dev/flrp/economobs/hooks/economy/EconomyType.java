package dev.flrp.economobs.hooks.economy;

public enum EconomyType {

    VAULT,
    TOKEN_MANAGER,
    PLAYER_POINTS;

    public static EconomyType getByName(String name) {
        for(EconomyType economyType : values()) {
            if(economyType.name().equalsIgnoreCase(name)) return economyType;
        }
        return VAULT;
    }

}
