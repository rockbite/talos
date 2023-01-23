package com.talosvfx.talos.editor.addons.scene.events;

import com.badlogic.gdx.utils.Json;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import com.talosvfx.talos.editor.notifications.TalosEvent;
import com.talosvfx.talos.runtime.scene.components.AComponent;
import lombok.Data;

@Data
public class ComponentUpdated implements TalosEvent {

    private GameObjectContainer container;
    private GameObject parent;
    private AComponent component;
    private boolean rapid;

    private boolean notifyUI;

    @Override
    public void reset () {
        component = null;
        rapid = false;
        notifyUI = false;
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

    public boolean isRapid() {
        return rapid;
    }
}
