package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.rockbite.tools.talos.runtime.modules.ColorModule;
import com.rockbite.tools.talos.runtime.modules.Module;

public class ColorModuleWrapper extends ModuleWrapper<ColorModule> {

    @Override
    protected void configureSlots() {
        final VisTextField rField = addInputSlotWithTextField("R: ", 0);
        final VisTextField gField = addInputSlotWithTextField("G: ", 1);
        final VisTextField bField = addInputSlotWithTextField("B: ", 2);

        rField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float r = floatFromText(rField);
                module.setR(r);
            }
        });

        gField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float g = floatFromText(gField);
                module.setG(g);
            }
        });

        bField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float b = floatFromText(bField);
                module.setB(b);
            }
        });

        addOutputSlot("position", 0);
    }

    @Override
    protected float reportPrefWidth() {
        return 210;
    }
}
