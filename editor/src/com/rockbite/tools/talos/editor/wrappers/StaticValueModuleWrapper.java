package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.rockbite.tools.talos.editor.widgets.FloatInputWidget;
import com.rockbite.tools.talos.runtime.modules.StaticValueModule;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class StaticValueModuleWrapper extends ModuleWrapper<StaticValueModule> {

    private FloatInputWidget floatInput;

    public StaticValueModuleWrapper() {
        super();
    }

    @Override
    public void setModule(StaticValueModule module) {
        super.setModule(module);
        module.setStaticValue(1f);
    }

    @Override
    public void attachModuleToMyOutput(ModuleWrapper moduleWrapper, int mySlot, int targetSlot) {
        super.attachModuleToMyOutput(moduleWrapper, mySlot, targetSlot);

        floatInput.setFlavour(module.getOutputValue().getFlavour());
    }

    @Override
    public void setSlotInactive(int slotTo, boolean isInput) {
        super.setSlotInactive(slotTo, isInput);
        if(!isInput) {
            floatInput.setFlavour(NumericalValue.Flavour.REGULAR);
            floatInput.setText("Number");
        }
    }

    @Override
    protected float reportPrefWidth() {
        return 150;
    }

    @Override
    protected void configureSlots() {
        floatInput = new FloatInputWidget("Number", getSkin());

        contentWrapper.add(floatInput).left().padLeft(4);
        contentWrapper.add().expandX();


        addOutputSlot("output", 0);


        floatInput.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = floatInput.getValue();
                module.setStaticValue(value);
            }
        });
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        floatInput.setValue(module.getStaticValue());
    }
}
