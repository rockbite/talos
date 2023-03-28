package com.talosvfx.talos.editor.addons.scene.events.save;

import com.talosvfx.talos.editor.notifications.TalosEvent;
import lombok.Getter;
import lombok.Setter;

public class ExportRequest implements TalosEvent {

    @Getter@Setter
    private boolean optimized;
    @Override
    public void reset() {
        optimized = false;
    }
}
