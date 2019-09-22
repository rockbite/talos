package com.rockbite.tools.talos.editor.wrappers;


import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.rockbite.tools.talos.editor.widgets.FloatInputWidget;
import com.rockbite.tools.talos.editor.widgets.FloatRangeInputWidget;
import com.rockbite.tools.talos.runtime.modules.RandomRangeModule;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class RandomRangeModuleWrapper extends ModuleWrapper<RandomRangeModule> {

    FloatRangeInputWidget inputRange;

    @Override
    public void attachModuleToMyOutput(ModuleWrapper moduleWrapper, int mySlot, int targetSlot) {
        super.attachModuleToMyOutput(moduleWrapper, mySlot, targetSlot);

        inputRange.setFlavour(module.getOutputValue().getFlavour());
    }

    @Override
    public void setSlotInactive(int slotTo, boolean isInput) {
        super.setSlotInactive(slotTo, isInput);
        if(!isInput) {
            inputRange.setFlavour(NumericalValue.Flavour.REGULAR);
            inputRange.setText("Min", "Max");
        }
    }

    @Override
    protected float reportPrefWidth() {
        return 250;
    }

    @Override
    protected void configureSlots() {

        addOutputSlot("result", 0);

        inputRange = new FloatRangeInputWidget("Min", "Max", getSkin());
        contentWrapper.add(inputRange).left().padTop(0).padLeft(4).expandX();

        leftWrapper.add(new Table()).expandY();
        rightWrapper.add(new Table()).expandY();

        inputRange.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateValues();
            }
        });

    }

    private void updateValues() {
        float min = inputRange.getMinValue();
        float max = inputRange.getMaxValue();

        module.setMinMax(min, max);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        setData(module.getMin(), module.getMax());
    }

    public void setData(float min, float max) {
        inputRange.setValue(min, max);
        updateValues();
    }
}
