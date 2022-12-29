package com.talosvfx.talos.editor.project2.apps.preferences;

public interface ContainerOfPrefs<T extends AppPrefs.AppPreference> {

    void applyFromPreferences(T prefs);


    /**
     * Get current preference. App should query the state of its widget and update preferences to be stored.
     * @see  com.talosvfx.talos.editor.project2.apps.SceneEditorApp for example implementation.
     */
    T getPrefs();
}
