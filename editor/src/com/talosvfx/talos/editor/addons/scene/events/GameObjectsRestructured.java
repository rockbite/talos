package com.talosvfx.talos.editor.addons.scene.events;

import com.badlogic.gdx.utils.ObjectSet;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import com.talosvfx.talos.editor.notifications.events.AbstractContextRequiredEvent;

public class GameObjectsRestructured extends AbstractContextRequiredEvent<GameObjectContainer> {
    public ObjectSet<GameObject> targets;

    public GameObjectsRestructured () {
        targets = new ObjectSet<>(16);
    }

    public GameObjectsRestructured set (GameObjectContainer container, ObjectSet<GameObject> targets) {
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
