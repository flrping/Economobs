package dev.flrp.economobs.configuration;

public enum StackerType {

    NONE,
    WILDSTACKER,
    STACKMOB;

    public static StackerType getName(String name) {
        try {
            return StackerType.valueOf(name);
        } catch (IllegalArgumentException e) {
            Locale.log("&cStacker can't be recognized in config.yml. Listening to regular deaths.");
            return NONE;
        }
    }

}
