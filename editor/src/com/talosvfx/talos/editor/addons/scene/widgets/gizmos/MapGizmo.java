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

public class MapGizmo extends TransformGizmo {

    public MapGizmo () {
        super();
    }

    @Override
    public void setSelected (boolean selected) {
        super.setSelected(selected);
        if (isSelected()) {
            SceneEditorWorkspace.getInstance().customGrid = true;
            setupForCustomGrid(gameObject);
        } else {
            SceneEditorWorkspace.getInstance().customGrid = false;
        }
    }

    private void setupForCustomGrid (GameObject gameObject) {

        SceneEditorWorkspace.GridProperties gridProperties = SceneEditorWorkspace.getInstance().gridProperties;

        Supplier<float[]> sizeProvider = new Supplier<float[]>() {
            @Override
            public float[] get () {
                MapComponent mapComponent = gameObject.getComponent(MapComponent.class);
                TalosLayer selectedLayer = mapComponent.selectedLayer;

                if (selectedLayer == null) {
                    gridProperties.noLayerSelected = true;
                    return new float[]{1,1};
                } else {
                    gridProperties.noLayerSelected = false;
                    return new float[]{selectedLayer.getTileSizeX(), selectedLayer.getTileSizeY()};
                }

            }
        };
        gridProperties.sizeProvider = sizeProvider;
    }
}
