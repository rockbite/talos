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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.utils.ScreenshotService;
import com.talosvfx.talos.runtime.values.ColorPoint;
import com.talosvfx.talos.editor.widgets.GradientWidget;
import com.talosvfx.talos.runtime.modules.GradientColorModule;
import com.talosvfx.talos.runtime.modules.InterpolationModule;

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

                SharedResources.stage.addActor(picker.fadeIn());
                picker.toFront();

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
        gradientWidget.updateGradientData();
    }


    @Override
    public void act (float delta) {
        super.act(delta);

        ScreenshotService.testForPicker(picker);
    }
}
