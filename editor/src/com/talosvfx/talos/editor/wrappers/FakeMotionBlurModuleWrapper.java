package com.talosvfx.talos.editor.wrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.project2.TalosVFXUtils;
import com.talosvfx.talos.editor.widgets.FloatRangeInputWidget;
import com.talosvfx.talos.runtime.vfx.modules.FakeMotionBlurModule;

public class FakeMotionBlurModuleWrapper extends ModuleWrapper<FakeMotionBlurModule> {

    FloatRangeInputWidget velocityMinMax;
    FloatRangeInputWidget sizeMinMax;

    @Override
    protected void configureSlots() {
        addInputSlot("velocity", FakeMotionBlurModule.VELOCITY);
        addOutputSlot("size", FakeMotionBlurModule.SIZE);


        velocityMinMax = new FloatRangeInputWidget("Vel Min", "Vel Max", getSkin(), true);
        sizeMinMax = new FloatRangeInputWidget("Size Min", "Size Max", getSkin(), true);


        contentWrapper.add(velocityMinMax).center().padTop(20).padLeft(4).row();
        contentWrapper.add(sizeMinMax).center().padTop(0).padLeft(4).row();

        leftWrapper.add(new Table()).expandY();
        rightWrapper.add(new Table()).expandY();

        velocityMinMax.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setVelocityMin(velocityMinMax.getMinValue());
                module.setVelocityMax(velocityMinMax.getMaxValue());
            }
        });

        sizeMinMax.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setSizeMin(sizeMinMax.getMinValue());
                module.setSizeMax(sizeMinMax.getMaxValue());
            }
        });
    }

    @Override
    protected String getOverrideTitle() {
        return TalosVFXUtils.moduleNames.get(this.getClass());
    }

    @Override
    protected float reportPrefWidth() {
        return 150;
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        velocityMinMax.setValue(module.getVelocityMin(), module.getVelocityMax());
        sizeMinMax.setValue(module.getSizeMin(), module.getSizeMax());
    }
}
