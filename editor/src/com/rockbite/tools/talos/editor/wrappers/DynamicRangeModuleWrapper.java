package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.rockbite.tools.talos.editor.widgets.CurveDataProvider;
import com.rockbite.tools.talos.editor.widgets.CurveWidget;
import com.rockbite.tools.talos.editor.widgets.FloatRangeInputWidget;
import com.rockbite.tools.talos.runtime.Slot;
import com.rockbite.tools.talos.runtime.modules.*;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

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
    public Class<? extends Module> getSlotsPreferredModule(Slot slot) {
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
