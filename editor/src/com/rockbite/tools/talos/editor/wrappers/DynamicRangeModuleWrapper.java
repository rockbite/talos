package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.rockbite.tools.talos.editor.widgets.CurveWidget;
import com.rockbite.tools.talos.editor.widgets.FloatRangeInputWidget;
import com.rockbite.tools.talos.runtime.modules.DynamicRangeModule;
import com.rockbite.tools.talos.runtime.modules.InterpolationModule;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class DynamicRangeModuleWrapper extends ModuleWrapper<DynamicRangeModule> {

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
    protected void configureSlots() {
        addInputSlot("alpha (0 to 1)", InterpolationModule.ALPHA);

        addOutputSlot("output", 0);

        Table container = new Table();

        highInput = new FloatRangeInputWidget("HMin", "HMax", getSkin());
        lowInput = new FloatRangeInputWidget("LMin", "LMax", getSkin());

        container.add(highInput).row();
        container.add().height(3).row();
        container.add(lowInput);

        contentWrapper.add(container).left().padTop(20).expandX().padLeft(4);

        curveWidget = new CurveWidget(getSkin());
        contentWrapper.add(curveWidget).left().height(119).width(200).padTop(23).padRight(3).padBottom(3);

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
    public void setModule(DynamicRangeModule module) {
        super.setModule(module);
        curveWidget.setModule(module);
    }

    @Override
    public void write(JsonValue value) {
        float lowMin = module.getLowMin();
        float lowMax = module.getLowMax();
        float highMin = module.getHightMin();
        float highMax = module.getHightMax();
        value.addChild("lowMin", new JsonValue(lowMin));
        value.addChild("lowMax", new JsonValue(lowMax));
        value.addChild("highMin", new JsonValue(highMin));
        value.addChild("highMax", new JsonValue(highMax));

        // now points
        Array<Vector2> points = module.getPoints();
        JsonValue arr = new JsonValue(JsonValue.ValueType.array);
        value.addChild("points", arr);
        for(Vector2 point: points) {
            JsonValue vec = new JsonValue(JsonValue.ValueType.array);
            vec.addChild(new JsonValue(point.x));
            vec.addChild(new JsonValue(point.y));
            arr.addChild(vec);
        }
    }

    @Override
    public void read(JsonValue value) {
        String lowMin = value.getString("lowMin");
        String lowMax = value.getString("lowMax");
        String highMin = value.getString("highMin");
        String highMax = value.getString("highMax");

        lowInput.setValue(floatFromText(lowMin), floatFromText(lowMax));
        highInput.setValue(floatFromText(highMin), floatFromText(highMax));

        updateValues();

        JsonValue points = value.get("points");
        module.getPoints().clear();
        for(JsonValue point: points) {
            module.createPoint(point.get(0).asFloat(), point.get(1).asFloat());
        }
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
    }
}
