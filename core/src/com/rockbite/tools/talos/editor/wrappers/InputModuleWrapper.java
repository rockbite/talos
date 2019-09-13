package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.IntMap;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.rockbite.tools.talos.runtime.InputModule;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.StaticValueModule;

public class InputModuleWrapper extends ModuleWrapper<InputModule> {

    IntMap<String> map;

    public InputModuleWrapper() {
        super();
    }

    @Override
    public void setModule(InputModule module) {
        super.setModule(module);
        module.setInput(ScopePayload.EMITTER_ALPHA);
    }

    @Override
    protected float reportPrefWidth() {
        return 250;
    }

    @Override
    protected void configureSlots() {
        map = new IntMap<>();
        map.put(ScopePayload.EMITTER_ALPHA, "Emitter.alpha");
        map.put(ScopePayload.PARTICLE_ALPHA, "Particle.alpha");


        final VisSelectBox<String> selectBox = addSelectBox(map.values());
        addOutputSlot("output", 0);


        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedString = selectBox.getSelected();
                int key = map.findKey(selectedString, false, 0);

                module.setInput(key);
            }
        });
    }
}
