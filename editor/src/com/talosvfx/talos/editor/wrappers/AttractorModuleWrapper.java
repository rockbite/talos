package com.talosvfx.talos.editor.wrappers;

import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.runtime.vfx.Slot;
import com.talosvfx.talos.runtime.vfx.modules.AbstractModule;
import com.talosvfx.talos.runtime.vfx.modules.AttractorModule;
import com.talosvfx.talos.runtime.vfx.modules.InterpolationModule;
import com.talosvfx.talos.runtime.vfx.modules.RandomRangeModule;

public class AttractorModuleWrapper extends ModuleWrapper<AttractorModule> {

    @Override
    protected void configureSlots() {
        addInputSlot("initial angle: ", AttractorModule.INITIAL_ANGLE);
        addInputSlot("initial velocity: ", AttractorModule.INITIAL_VELOCITY);
        addInputSlot("attraction point: ", AttractorModule.ATTRACTOR_POSITION);
        addInputSlot("particle life: ", AttractorModule.ALPHA);

        addOutputSlot("angle", AttractorModule.ANGLE);
        addOutputSlot("velocity", AttractorModule.VELOCITY);
    }


    @Override
    public Class<? extends AbstractModule>  getSlotsPreferredModule(Slot slot) {

        if(slot.getIndex() == AttractorModule.INITIAL_ANGLE) return RandomRangeModule.class;
        if(slot.getIndex() == AttractorModule.INITIAL_VELOCITY) return RandomRangeModule.class;
        if(slot.getIndex() == AttractorModule.ATTRACTOR_POSITION) return TalosMain.Instance().UIStage().getPreferred3DVectorClass();;
        if(slot.getIndex() == AttractorModule.ALPHA) return InterpolationModule.class;
        return null;
    }

    @Override
    protected float reportPrefWidth () {
        return 200;
    }
}
