package com.talosvfx.talos.editor.project2.apps;

public interface MemoryApp {

    /**
     * Apply preferences to the app. Set widget values from the preferences object.
     * @see com.talosvfx.talos.editor.project2.apps.SceneEditorApp for example implementation.
     * @param appPreferences
     */
    void applyPreferences(Object appPreferences);


    /**
     * Get current preference. App should query the state of its widget and update preferences to be stored.
     * @see  com.talosvfx.talos.editor.project2.apps.SceneEditorApp for example implementation.
     */
    Object getCurrentPreference();
}
