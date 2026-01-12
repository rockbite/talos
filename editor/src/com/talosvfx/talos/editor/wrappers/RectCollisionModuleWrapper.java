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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.VisUI;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.CheckboxWithZoom;
import com.talosvfx.talos.runtime.vfx.modules.RectCollisionModule;

public class RectCollisionModuleWrapper extends ModuleWrapper<RectCollisionModule> {

    private TextField xField;
    private TextField yField;
    private TextField widthField;
    private TextField heightField;

    private TextField restitutionField;
    private TextField frictionField;
    private TextField lifetimeReductionField;
    private CheckboxWithZoom localSpaceBox;

    @Override
    public void setModule(RectCollisionModule module) {
        super.setModule(module);
        xField.setText(module.getDefaultX() + "");
        yField.setText(module.getDefaultY() + "");
        widthField.setText(module.getDefaultWidth() + "");
        heightField.setText(module.getDefaultHeight() + "");
        restitutionField.setText(module.getRestitution() + "");
        frictionField.setText(module.getFriction() + "");
        lifetimeReductionField.setText(module.getLifetimeReduction() + "");
        localSpaceBox.setChecked(module.isLocalSpace());
    }

    @Override
    protected void configureSlots() {
        xField = addInputSlotWithTextField("X: ", RectCollisionModule.X);
        yField = addInputSlotWithTextField("Y: ", RectCollisionModule.Y);
        widthField = addInputSlotWithTextField("Width: ", RectCollisionModule.WIDTH);
        heightField = addInputSlotWithTextField("Height: ", RectCollisionModule.HEIGHT);

        xField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float x = floatFromText(xField);
                module.setX(x);
            }
        });

        yField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float y = floatFromText(yField);
                module.setY(y);
            }
        });

        widthField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float width = floatFromText(widthField);
                module.setWidth(width);
            }
        });

        heightField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float height = floatFromText(heightField);
                module.setHeight(height);
            }
        });

        addSeparator(true);

        // Physics properties
        restitutionField = addInputSlotWithTextField("Restitution: ", -1);
        frictionField = addInputSlotWithTextField("Friction: ", -1);
        lifetimeReductionField = addInputSlotWithTextField("Life Reduce: ", -1);

        restitutionField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float val = floatFromText(restitutionField);
                module.setRestitution(val);
            }
        });

        frictionField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float val = floatFromText(frictionField);
                module.setFriction(val);
            }
        });

        lifetimeReductionField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float val = floatFromText(lifetimeReductionField);
                module.setLifetimeReduction(val);
            }
        });

        // Local space checkbox
        localSpaceBox = new CheckboxWithZoom("local space", VisUI.getSkin());
        localSpaceBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setLocalSpace(localSpaceBox.isChecked());
            }
        });

        leftWrapper.add(localSpaceBox).left().expandX().padLeft(3).row();

        addOutputSlot("module", RectCollisionModule.MODULE);
    }

    @Override
    protected float reportPrefWidth() {
        return 220;
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        xField.setText(module.getDefaultX() + "");
        yField.setText(module.getDefaultY() + "");
        widthField.setText(module.getDefaultWidth() + "");
        heightField.setText(module.getDefaultHeight() + "");
        restitutionField.setText(module.getRestitution() + "");
        frictionField.setText(module.getFriction() + "");
        lifetimeReductionField.setText(module.getLifetimeReduction() + "");
        localSpaceBox.setChecked(module.isLocalSpace());
    }
}
