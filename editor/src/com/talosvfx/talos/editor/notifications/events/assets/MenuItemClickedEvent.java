package com.talosvfx.talos.editor.notifications.events.assets;

import com.talosvfx.talos.editor.notifications.TalosEvent;
import lombok.Getter;

public class MenuItemClickedEvent implements TalosEvent {

    @Getter
    private String path;

    @Getter
    private Object payload;

    public MenuItemClickedEvent set(String path, Object payload) {
        this.path = path;

        this.payload = payload;

        return this;
    }

    @Override
    public void reset() {
        path = null;
        payload = null;
    }
}
