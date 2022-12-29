package com.talosvfx.talos.editor.project2.apps.preferences;

import com.badlogic.gdx.math.Vector3;
import lombok.Data;

@Data
public class ViewportPreferences implements AppPrefs.AppPreference {
    public Vector3 cameraPos;
    public float cameraZoom;
}
