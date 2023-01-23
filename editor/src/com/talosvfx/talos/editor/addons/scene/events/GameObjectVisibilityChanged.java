package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;

public class GameObjectVisibilityChanged extends ContextRequiredEvent<GameObjectContainer> {

    private GameObject target;

    public GameObjectVisibilityChanged set (GameObjectContainer context, GameObject target) {
        setContext(context);
        this.target = target;

        return this;
    }

    public GameObject getTarget() {
        return target;
    }

    @Override
    public void reset () {
        target = null;
    }
}
