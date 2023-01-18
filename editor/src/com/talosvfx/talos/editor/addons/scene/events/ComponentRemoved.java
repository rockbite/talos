package com.talosvfx.talos.editor.addons.scene.events;

import com.badlogic.gdx.utils.Json;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import com.talosvfx.talos.editor.addons.scene.logic.componentwrappers.AComponent;
import com.talosvfx.talos.editor.notifications.TalosEvent;
import lombok.Data;

@Data
public class ComponentRemoved implements TalosEvent {

    private AComponent component;
    private GameObject gameObject;
    private GameObjectContainer container;

    @Override
    public void reset () {
        component = null;
    }

    @Override
    public boolean notifyThroughSocket () {
        return true;
    }

    @Override
    public String getEventType () {
        return "ComponentUpdated";
    }

    @Override
    public Json getAdditionalData (Json json) {
        GameObject parentGameObject = component.getGameObject();
        if (parentGameObject == null || component == null) {
            return json;
        }

        json.writeValue("GoUUID", parentGameObject.uuid.toString());
        json.writeValue("ComponentType", component.getClass().getSimpleName());

        return json;
    }

    @Override
    public Json getMainData (Json json) {
        json.writeValue("component", component);
        return json;
    }
}
