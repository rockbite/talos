/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.talosvfx.talos.editor.wrappers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.widgets.CurveDataProvider;
import com.talosvfx.talos.editor.widgets.CurveWidget;
import com.talosvfx.talos.editor.widgets.FloatRangeInputWidget;
import com.talosvfx.talos.runtime.vfx.Slot;
import com.talosvfx.talos.runtime.vfx.modules.AbstractModule;
import com.talosvfx.talos.runtime.vfx.modules.DynamicRangeModule;
import com.talosvfx.talos.runtime.vfx.modules.InputModule;
import com.talosvfx.talos.runtime.vfx.modules.InterpolationModule;
import com.talosvfx.talos.runtime.vfx.values.NumericalValue;

public class DynamicRangeModuleWrapper extends ModuleWrapper<DynamicRangeModule> implements CurveDataProvider {

    private CurveWidget curveWidget;

    private FloatRangeInputWidget lowInput;
    private FloatRangeInputWidget highInput;

    @Override
    public void attachModuleToMyOutput(ModuleWrapper moduleWrapper, int mySlot, int targetSlot) {
        super.attachModuleToMyOutput(moduleWrapper, mySlot, targetSlot);

        lowInput.setFlavour(module.getOutputValue().getFlavour());
        highInput.setFlavour(module.getOutputValue().getFlavour());
    }

    @Override
    public void setSlotInactive(int slotTo, boolean isInput) {
        super.setSlotInactive(slotTo, isInput);
        if(!isInput) {
            lowInput.setFlavour(NumericalValue.Flavour.REGULAR);
            highInput.setFlavour(NumericalValue.Flavour.REGULAR);
        }
    }

    @Override
    public Class<? extends AbstractModule> getSlotsPreferredModule(Slot slot) {
        if(slot.getIndex() == DynamicRangeModule.ALPHA) return InputModule.class;

        return null;
    }

    @Override
    protected void configureSlots() {
        addInputSlot("alpha (0 to 1)", InterpolationModule.ALPHA);

        addOutputSlot("output", 0);

        Table container = new Table();

        highInput = new FloatRangeInputWidget("HMin", "HMax", getSkin());
        lowInput = new FloatRangeInputWidget("LMin", "LMax", getSkin());

        lowInput.setValue(0, 0);
        highInput.setValue(1, 1);

        container.add(highInput).row();
        container.add().height(3).row();
        container.add(lowInput);

        contentWrapper.add(container).left().padTop(20).expandX().padLeft(4);

        curveWidget = new CurveWidget(getSkin());
        curveWidget.setDataProvider(this);
        contentWrapper.add(curveWidget).left().growY().width(200).padTop(23).padRight(3).padLeft(4).padBottom(3);

        leftWrapper.add(new Table()).expandY();
        rightWrapper.add(new Table()).expandY();

        highInput.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateValues();
            }
        });
        lowInput.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateValues();
            }
        });
    }

    @Override
    public void write(Json json) {
        super.write(json);

        json.writeValue("lowEquals", lowInput.getEqualsButton().isChecked());
        json.writeValue("lowMirror", lowInput.getMirrorButton().isChecked());

        json.writeValue("highEquals", highInput.getEqualsButton().isChecked());
        json.writeValue("highMirror", highInput.getMirrorButton().isChecked());
    }

    @Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);

        lowInput.getEqualsButton().setChecked(jsonData.getBoolean("lowEquals"));
        lowInput.getMirrorButton().setChecked(jsonData.getBoolean("lowMirror"));
        highInput.getEqualsButton().setChecked(jsonData.getBoolean("highEquals"));
        highInput.getMirrorButton().setChecked(jsonData.getBoolean("highMirror"));

		lowInput.setValue(module.getLowMin(), module.getLowMax());
		highInput.setValue(module.getHightMin(), module.getHightMax());

		updateValues();
	}


    @Override
    protected float reportPrefWidth() {
        return 390;
    }

    private void updateValues() {
        module.setMinMaxLow(lowInput.getMinValue(), lowInput.getMaxValue());
        module.setMinMaxHigh(highInput.getMinValue(), highInput.getMaxValue());
    }

    public void setData(float lowMin, float lowMax, float highMin, float highMax, Array<Vector2> points) {
        lowInput.setValue(lowMin, lowMax);
        highInput.setValue(highMin, highMax);

        module.getPoints().clear();
        for(Vector2 point: points) {
            module.createPoint(point.x, point.y);
        }

        updateValues();
    }

    @Override
    public Array<Vector2> getPoints() {
        if(module == null) return null;

        return module.getPoints();
    }

    @Override
    public void removePoint(int index) {
        if(module == null) return;
        module.removePoint(index);
    }

    @Override
    public int createPoint(float x, float y) {
        if(module == null) return 0;
        return module.createPoint(x, y);
    }
}
