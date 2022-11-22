package com.talosvfx.talos.editor.addons.scene.events;

import com.badlogic.gdx.utils.Json;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.AComponent;
import com.talosvfx.talos.editor.notifications.Notifications;

public class ComponentUpdated implements Notifications.Event {

    private AComponent component;
    private boolean rapid;

    private boolean notifyUI;

    @Override
    public void reset () {
        component = null;
        rapid = false;
        notifyUI = false;
    }

    public Notifications.Event set (AComponent component, boolean rapid, boolean notifyUI) {
        this.rapid = rapid;
        this.component = component;
        this.notifyUI = notifyUI;
        return this;
    }

    public Notifications.Event set (AComponent component) {
        return set(component, false, true);
    }

    public Notifications.Event set (AComponent component, boolean rapid) {
        return set(component, rapid, true);
    }

    public AComponent getComponent () {
        return component;
    }

    public boolean wasRapid () {
        return rapid;
    }

    public boolean isNotifyUI () {
        return notifyUI;
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
        GameObject parentGameObject = SceneEditorWorkspace.getInstance().getGOWith(component);
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
