package com.talosvfx.talos.editor.project2.apps.preferences;

import com.talosvfx.talos.editor.widgets.ui.ViewportViewSettings;
import lombok.Data;

@Data
public class ViewportSettingPreferences {
    public ViewportViewSettings.CurrentCameraType currentCameraType;

    public float width;
    public float fov;
    public float near;
    public float far;


    public boolean is3D;
    public boolean positiveZUp;

    public boolean showAxis;
    public boolean showGrid;
    public boolean gridOnTop;
    public float gridSize;
}
