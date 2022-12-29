package com.talosvfx.talos.editor.project2.apps.preferences;

import com.badlogic.gdx.math.Vector3;

/**
 * Subclasses should be implemented in the app itself, so AppManager can locate them.
 */
public abstract class ViewportPreferences implements AppPrefs.AppPreference {
    public Vector3 cameraPos;
    public float cameraZoom;
}
