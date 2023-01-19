package com.talosvfx.talos.editor.notifications.events.deprecatedparticles;

import com.talosvfx.talos.editor.notifications.TalosEvent;
import com.talosvfx.talos.editor.wrappers.IDragPointProvider;
import com.talosvfx.talos.editor.wrappers.ModuleWrapper;
import lombok.Data;


@Data
public class RegisterDragPoints implements TalosEvent {

    private IDragPointProvider registerForDragPoints;
    @Override
    public void reset () {

    }
}
