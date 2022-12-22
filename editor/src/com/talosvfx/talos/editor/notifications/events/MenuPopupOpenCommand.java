package com.talosvfx.talos.editor.notifications.events;

import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.editor.notifications.TalosEvent;
import lombok.Getter;

public class MenuPopupOpenCommand implements TalosEvent {


    @Getter
    private String path;

    @Getter
    private Vector2 preferredPos = new Vector2();

    public MenuPopupOpenCommand set(String path, Vector2 preferredPos) {
        this.path = path;
        this.preferredPos.set(preferredPos);

        return this;
    }

    @Override
    public void reset() {
        path = null;
        preferredPos.setZero();
    }
}
