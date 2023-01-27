package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.editor.notifications.TalosEvent;
import com.talosvfx.talos.runtime.scene.components.PaintSurfaceComponent;

public class PaintSurfaceResize implements TalosEvent {

    public PaintSurfaceComponent component;
    @Override
    public void reset() {
        component = null;
    }

    public PaintSurfaceResize set(PaintSurfaceComponent component) {
        this.component = component;
        return this;
    }
}
