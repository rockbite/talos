package com.talosvfx.talos.editor.addons.scene.events.commands;

import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.notifications.TalosEvent;
import lombok.Getter;

// todo: this command should contain the context of scene or prefab or whoever was the container of this GO
public class GONameChangeCommand implements TalosEvent {

    @Getter
    private GameObject go;
    @Getter
    private String suggestedName;

    public GONameChangeCommand set(GameObject go, String suggestedName) {
        this.go = go;
        this.suggestedName = suggestedName;

        return this;
    }

    @Override
    public void reset() {

    }
}
