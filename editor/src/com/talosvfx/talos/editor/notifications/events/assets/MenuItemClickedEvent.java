package com.talosvfx.talos.editor.notifications.events.assets;

import com.talosvfx.talos.editor.notifications.TalosEvent;
import lombok.Getter;

public class MenuItemClickedEvent implements TalosEvent {

    @Getter
    private String path;

    public MenuItemClickedEvent set(String path) {
        this.path = path;

        return this;
    }

    @Override
    public void reset() {
        path = null;
    }
}
