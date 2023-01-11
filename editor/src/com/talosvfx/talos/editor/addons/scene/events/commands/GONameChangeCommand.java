package com.talosvfx.talos.editor.addons.scene.events.commands;

import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.GameObjectContainer;
import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;
import lombok.Getter;

public class GONameChangeCommand extends ContextRequiredEvent<GameObjectContainer> {

    @Getter
    private GameObject go;
    @Getter
    private String suggestedName;

    public GONameChangeCommand set(GameObjectContainer context, GameObject go, String suggestedName) {
        setContext(context);

        this.go = go;
        this.suggestedName = suggestedName;

        return this;
    }

    @Override
    public void reset() {

    }
}
