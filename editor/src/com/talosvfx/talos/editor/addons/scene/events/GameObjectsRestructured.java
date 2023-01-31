package com.talosvfx.talos.editor.addons.scene.events;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;
import com.talosvfx.talos.editor.notifications.TalosEvent;
import com.talosvfx.talos.editor.notifications.events.AbstractContextRequiredEvent;

public class GameObjectsRestructured extends AbstractContextRequiredEvent<GameObjectContainer> {
    public Array<GameObject> targets;

    public GameObjectsRestructured () {
        targets = new Array<>(false, 16);
    }

    public GameObjectsRestructured set (GameObjectContainer container, Array<GameObject> targets) {
        this.setContext(container);

        this.targets.clear();
        this.targets.addAll(targets);

        return this;
    }

    @Override
    public void reset () {
        super.reset();
        targets.clear();
    }
}
