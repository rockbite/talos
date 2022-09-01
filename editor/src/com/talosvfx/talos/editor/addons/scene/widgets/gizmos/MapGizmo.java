package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;

import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.events.ComponentUpdated;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.MapComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.maps.TalosLayer;
import com.talosvfx.talos.editor.notifications.Notifications;

import java.util.function.Supplier;

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
