package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;

import com.talosvfx.talos.runtime.scene.GameObject;
public class MapGizmo extends Gizmo {

    public MapGizmo () {
        super();
    }

    @Override
    public void setSelected (boolean selected) {
        super.setSelected(selected);
        if (isSelected()) {
            setupForCustomGrid(gameObject);
        } else {
        }
    }

    private void setupForCustomGrid (GameObject gameObject) {


    }
}
