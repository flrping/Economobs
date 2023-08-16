package dev.flrp.economobs.hooks.stacker;

public interface StackerProvider {

    /**
     * Register the events for the stacker hook.
     */
    void registerEvents();

    /**
     * Unregister the events for the stacker hook.
     */
    void unregisterEvents();

}
