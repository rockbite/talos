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
import com.rockbite.tools.talos.runtime.modules.DynamicRangeModule;
import com.rockbite.tools.talos.runtime.modules.InterpolationModule;

public class DynamicRangeModuleWrapper extends ModuleWrapper<DynamicRangeModule> {

    private CurveWidget curveWidget;

    private VisTextField lowMinField;
    private VisTextField lowMaxField;
    private VisTextField highMinField;
    private VisTextField highMaxField;

    @Override
    protected void configureSlots() {
        addInputSlot("alpha (0 to 1)", InterpolationModule.ALPHA);

        addOutputSlot("output", 0);

        Table container = new Table();

        createMinMax(container, "High", true);
        createMinMax(container, "Low", false);

        contentWrapper.add(container).left().padTop(20).expandX();

        curveWidget = new CurveWidget(getSkin());
        contentWrapper.add(curveWidget).left().height(100).width(200).padTop(23).padRight(3).padBottom(3);

        leftWrapper.add(new Table()).expandY();
        rightWrapper.add(new Table()).expandY();
    }

    public void createMinMax(Table container, String text, final boolean isHigh) {
        VisTable table = new VisTable();

        // let's create our fields
        VisLabel label = new VisLabel(text);
        final VisTextField minLabel = new VisTextField("0");
        final VisTextField maxLabel = new VisTextField("100");

        if(isHigh) {
            highMinField = minLabel;
            highMaxField = maxLabel;
        } else {
            lowMinField = minLabel;
            lowMaxField = maxLabel;
        }

        table.add(label).left();
        table.row().padTop(4);

        Table eWrap = new Table();
        eWrap.add(minLabel).width(60).padRight(5);
        eWrap.add(maxLabel).width(60);
        table.add(eWrap).expandX().left();

        table.row();

        container.add(table).left().padTop(0).expandX().padLeft(5);
        container.row();

        minLabel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateValues(minLabel, maxLabel, isHigh);
            }
        });
        maxLabel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateValues(minLabel, maxLabel, isHigh);
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
        lowMinField.setText(lowMin);
        lowMaxField.setText(lowMax);
        highMinField.setText(highMin);
        highMaxField.setText(highMax);
        updateValues(lowMinField, lowMaxField, false);
        updateValues(highMinField, highMaxField, true);

        JsonValue points = value.get("points");
        for(JsonValue point: points) {
            module.createPoint(point.get(0).asFloat(), point.get(1).asFloat());
        }
    }

    @Override
    protected float reportPrefWidth() {
        return 350;
    }

    private void updateValues(VisTextField minLabel, VisTextField maxLabel, boolean isHigh) {
        float min = floatFromText(minLabel);
        float max = floatFromText(maxLabel);

        if(isHigh) {
            module.setMinMaxHigh(min, max);
        } else {
            module.setMinMaxLow(min, max);
        }
    }
}
