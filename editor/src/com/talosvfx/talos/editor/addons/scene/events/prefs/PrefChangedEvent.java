package com.talosvfx.talos.editor.addons.scene.events.prefs;

import com.talosvfx.talos.editor.notifications.TalosEvent;
import lombok.Data;

@Data
public class PrefChangedEvent implements TalosEvent {

    private String id;
    @Override
    public void reset () {

    }
}
