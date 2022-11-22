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
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.widgets.CurveDataProvider;
import com.talosvfx.talos.editor.widgets.CurveWidget;
import com.talosvfx.talos.editor.widgets.ShapeInputWidget;
import com.talosvfx.talos.runtime.modules.InterpolationModule;
import com.talosvfx.talos.runtime.modules.OffsetModule;

public class OffsetModuleWrapper extends ModuleWrapper<OffsetModule> implements CurveDataProvider {

    private ShapeInputWidget lowShape;
    private ShapeInputWidget highShape;

    private ImageButton equalsButton;

    private CurveWidget curveWidget;

    private Vector2 pos = new Vector2();
    private Vector2 size = new Vector2();

    private boolean lockUpdate = false;

    @Override
    protected float reportPrefWidth() {
        return 250;
    }

    @Override
    public void setModule(OffsetModule module) {
        super.setModule(module);
        updateModuleDataFromWidgets();
    }

    @Override
    protected void configureSlots() {

        addInputSlot("alpha (0 to 1)", InterpolationModule.ALPHA);

        lowShape = new ShapeInputWidget(getSkin());
        highShape = new ShapeInputWidget(getSkin());
        curveWidget = new CurveWidget(getSkin());
        curveWidget.setDataProvider(this);

        Table midTable = new Table();
        equalsButton = new ImageButton(getSkin(), "chain");
        equalsButton.setChecked(true);
        midTable.add(equalsButton);

        contentWrapper.add(lowShape).width(100).pad(5);
        contentWrapper.add(midTable).width(30);
        contentWrapper.add(highShape).width(100).pad(5);
        contentWrapper.row();
        contentWrapper.add(curveWidget).width(240).height(100).padTop(0).colspan(3).padBottom(3);

        contentWrapper.padTop(15);

        addOutputSlot("output", OffsetModule.OUTPUT);
        leftWrapper.add(new Table()).expandY();
        rightWrapper.add(new Table()).expandY();

        if(module != null) {
            updateModuleDataFromWidgets();
        }

        equalsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                lowShape.getShapePos(pos);
                lowShape.getShapeSize(size);
                if(equalsButton.isChecked()) {
                    highShape.setScaleVal(lowShape.getScale());
                    highShape.setPos(pos);
                    highShape.setShapeSize(size);
                }
            }
        });

        lowShape.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                lowShape.getShapePos(pos);
                lowShape.getShapeSize(size);
                if(equalsButton.isChecked()) {
                    highShape.setScaleVal(lowShape.getScale());
                    highShape.setPos(pos);
                    highShape.setShapeSize(size);
                }

                updateModuleDataFromWidgets();
            }
        });

        highShape.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                highShape.getShapePos(pos);
                highShape.getShapeSize(size);
                if(equalsButton.isChecked()) {
                    lowShape.setScaleVal(highShape.getScale());
                    lowShape.setPos(pos);
                    lowShape.setShapeSize(size);
                }

                updateModuleDataFromWidgets();
            }
        });
    }

    private void updateModuleDataFromWidgets() {
        if(!lockUpdate) {
            lowShape.getShapePos(pos);
            module.setLowPos(pos);
            lowShape.getShapeSize(size);
            module.setLowSize(size);
            highShape.getShapePos(pos);
            module.setHighPos(pos);
            highShape.getShapeSize(size);
            module.setHighSize(size);

            module.setLowShape(lowShape.getShape());
            module.setHighShape(highShape.getShape());

            module.setLowEdge(lowShape.isEdge());
            module.setHighEdge(highShape.isEdge());

            module.setLowSide(lowShape.getSide());
            module.setHighSide(highShape.getSide());
        }
    }

    public void updateWidgetsFromModuleData() {
        lockUpdate = true;
        module.getLowPos(pos);
        lowShape.setPos(pos);
        module.getHighPos(pos);
        highShape.setPos(pos);

        module.getLowSize(size);
        lowShape.setShapeSize(size);
        module.getHighSize(size);
        highShape.setShapeSize(size);

        // shape edge and side
        lowShape.setShape(module.getLowShape());
        highShape.setShape(module.getHighShape());
        lowShape.setEdge(module.getLowEdge());
        highShape.setEdge(module.getLowEdge());
        highShape.setSide(module.getLowSide());
        highShape.setSide(module.getHighSide());
        lockUpdate = false;
    }

    @Override
    public Array<Vector2> getPoints() {
        return module.getPoints();
    }

    @Override
    public void removePoint(int index) {
        module.removePoint(index);
    }

    @Override
    public int createPoint(float x, float y) {
        return module.createPoint(x, y);
    }

    public void setEquals(boolean eequals) {
        equalsButton.setChecked(eequals);
    }

    public void setPoints(Array<Vector2> points) {
        module.setPoints(points);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        lockUpdate = true;
        super.read(json, jsonData);

        equalsButton.setChecked(jsonData.getBoolean("equals"));

        lowShape.setScaleVal(jsonData.getFloat("lowScale"));
        highShape.setScaleVal(jsonData.getFloat("highScale"));

        lowShape.setShape(jsonData.getInt("lowShape", 0));
        highShape.setShape(jsonData.getInt("highShape", 0));
        lowShape.setSide(jsonData.getInt("lowSide", 0));
        highShape.setSide(jsonData.getInt("highSide", 0));

        updateWidgetsFromModuleData();
        lockUpdate = false;
    }

    @Override
    public void write(Json json) {
        super.write(json);

        json.writeValue("lowScale", lowShape.getScale());
        json.writeValue("highScale", highShape.getScale());

        json.writeValue("lowShape", lowShape.getShape());
        json.writeValue("highShape", highShape.getShape());
        json.writeValue("lowSide", lowShape.getSide());
        json.writeValue("highSide", highShape.getSide());

        json.writeValue("equals", equalsButton.isChecked());
    }

    public void setScaleValues(float lowScl, float highScl) {
        lowShape.setScaleVal(lowScl);
        highShape.setScaleVal(highScl);
    }
}
