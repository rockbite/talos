package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import com.kotcrab.vis.ui.widget.color.ColorPickerListener;
import com.rockbite.tools.talos.editor.tools.ColorPoint;
import com.rockbite.tools.talos.editor.widgets.GradientWidget;
import com.rockbite.tools.talos.runtime.modules.CurveModule;
import com.rockbite.tools.talos.runtime.modules.GradientColorModule;
import com.rockbite.tools.talos.runtime.modules.InterpolationModule;

public class GradientColorModuleWrapper extends ModuleWrapper<GradientColorModule> {

    GradientWidget gradientWidget;

    private ColorPicker picker;

    @Override
    protected void configureSlots() {
        addInputSlot("alpha (0 to 1)", InterpolationModule.ALPHA);
        addOutputSlot("output", 0);

        gradientWidget = new GradientWidget(getSkin());
        contentWrapper.add(gradientWidget).expandX().fillX().growX().height(60).padTop(25).padRight(3).padBottom(3);

        leftWrapper.add(new Table()).expandY();
        rightWrapper.add(new Table()).expandY();

        picker = new ColorPicker();

        gradientWidget.setListener(new GradientWidget.GradientWidgetListener() {
            @Override
            public void colorPickerShow(final ColorPoint point) {
                picker.setListener(null);
                picker.setColor(point.color);

                getStage().addActor(picker.fadeIn());

                picker.setListener(new ColorPickerAdapter() {
                    @Override
                    public void changed(Color newColor) {
                        super.changed(newColor);

                        point.color.set(newColor);
                        gradientWidget.updateGradientData();
                    }
                });
            }
        });

        picker.padTop(32);
        picker.padLeft(16);
        picker.setHeight(330);
        picker.setWidth(430);
        picker.padRight(26);
    }

    @Override
    public void setModule(GradientColorModule module) {
        super.setModule(module);
        gradientWidget.setModule(module);
    }

    @Override
    public void write(JsonValue value) {
        Array<ColorPoint> points = module.getPoints();
        JsonValue arr = new JsonValue(JsonValue.ValueType.array);
        value.addChild("points", arr);
        for(ColorPoint point: points) {
            JsonValue vec = new JsonValue(JsonValue.ValueType.array);
            vec.addChild(new JsonValue(point.color.r));
            vec.addChild(new JsonValue(point.color.g));
            vec.addChild(new JsonValue(point.color.b));
            vec.addChild(new JsonValue(point.pos));
            arr.addChild(vec);
        }
    }

    @Override
    public void read(JsonValue value) {
        JsonValue points = value.get("points");
        module.getPoints().clear();
        for(JsonValue point: points) {
            Color color = new Color();
            color.set(point.get(0).asFloat(), point.get(1).asFloat(), point.get(2).asFloat(), 1f);
            module.createPoint(color, point.get(3).asFloat());
        }
        gradientWidget.updateGradientData();
    }

    @Override
    protected float reportPrefWidth() {
        return 350;
    }

    public void setData(Array<ColorPoint> points) {
        module.setPoints(points);
    }
}
