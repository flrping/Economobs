package dev.flrp.economobs.hooks.stacker;

import dev.flrp.economobs.configuration.Locale;

public enum StackerType {

    NONE,
    WILDSTACKER,
    STACKMOB,
    ROSESTACKER,
    ULTIMATESTACKER;

    public static StackerType getByName(String name) {
        for(StackerType stackerType : values()) {
            if(stackerType.name().equalsIgnoreCase(name)) return stackerType;
        }
        return NONE;
    }

}
