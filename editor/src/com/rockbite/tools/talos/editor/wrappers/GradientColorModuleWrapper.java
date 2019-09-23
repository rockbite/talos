package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import com.rockbite.tools.talos.runtime.values.ColorPoint;
import com.rockbite.tools.talos.editor.widgets.GradientWidget;
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
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
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
