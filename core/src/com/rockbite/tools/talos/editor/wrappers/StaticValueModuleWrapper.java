package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.rockbite.tools.talos.runtime.StaticValueModule;

import java.awt.*;

public class StaticValueModuleWrapper extends ModuleWrapper<StaticValueModule> {

    public StaticValueModuleWrapper() {
        super();
    }

    @Override
    public void setModule(StaticValueModule module) {
        super.setModule(module);
        module.setStaticValue(0f);
    }

    @Override
    protected float reportPrefWidth() {
        return 250;
    }

    @Override
    protected void configureSlots() {
        final VisTextField textField = addTextField("0");
        addOutputSlot("output", 0);


        textField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = floatFromText(textField);

                module.setStaticValue(value);
            }
        });
    }
}
