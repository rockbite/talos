package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;

public class GameObjectCreated extends ContextRequiredEvent<GameObjectContainer> {

    private GameObject target;

    public GameObjectCreated set (GameObjectContainer context, GameObject target) {
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
