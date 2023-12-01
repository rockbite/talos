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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.talosvfx.talos.runtime.vfx.modules.EmConfigModule;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.CheckboxWithZoom;

public class EmConfigModuleWrapper extends ModuleWrapper<EmConfigModule> {

    CheckboxWithZoom additiveBox;
    CheckboxWithZoom blendAddBox;
    CheckboxWithZoom attachedBox;
    CheckboxWithZoom continuousBox;
    CheckboxWithZoom alignedBox;
    CheckboxWithZoom immortalBox;
    CheckboxWithZoom youngestInBackBox;

    boolean lockListeners = false;

    @Override
    protected void configureSlots() {
        addOutputSlot("config", EmConfigModule.OUTPUT);

        additiveBox = new CheckboxWithZoom("additive", VisUI.getSkin());
        blendAddBox = new CheckboxWithZoom("blendadd", VisUI.getSkin());
        attachedBox = new CheckboxWithZoom("attached", VisUI.getSkin());
        continuousBox = new CheckboxWithZoom("continuous", VisUI.getSkin());
        alignedBox = new CheckboxWithZoom("aligned", VisUI.getSkin());
        immortalBox = new CheckboxWithZoom("immortal", VisUI.getSkin());
        youngestInBackBox = new CheckboxWithZoom("youngestInBackBox", VisUI.getSkin());

        Table form = new Table();

        form.add(additiveBox).left().padLeft(3);
        form.row();
        form.add(blendAddBox).left().padLeft(3);
        form.row();
        form.add(attachedBox).left().padLeft(3);
        form.row();
        form.add(continuousBox).left().padLeft(3);
        form.row();
        form.add(alignedBox).left().padLeft(3);
        form.row();
        form.add(immortalBox).left().padLeft(3);
        form.row();
        form.add(youngestInBackBox).left().padLeft(3);

        contentWrapper.add(form).left();
        contentWrapper.add().expandX();

        rightWrapper.add().expandY();

        additiveBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                fromUIToData();
            }
        });
        blendAddBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                fromUIToData();
            }
        });
        attachedBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                fromUIToData();
            }
        });
        continuousBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                fromUIToData();
            }
        });
        alignedBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                fromUIToData();
            }
        });
        immortalBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                fromUIToData();
            }
        });
        youngestInBackBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                fromUIToData();
            }
        });
    }

    @Override
    public void setModule(EmConfigModule module) {
        super.setModule(module);
        fromDataToUI();
    }

    public void fromUIToData() {
        if(!lockListeners) {
            module.getUserValue().additive = additiveBox.isChecked();
            module.getUserValue().isBlendAdd = blendAddBox.isChecked();
            module.getUserValue().attached = attachedBox.isChecked();
            module.getUserValue().continuous = continuousBox.isChecked();
            module.getUserValue().aligned = alignedBox.isChecked();
            module.getUserValue().immortal = immortalBox.isChecked();
            module.getUserValue().youngestInBack = youngestInBackBox.isChecked();

        }
    }

    public void fromDataToUI() {
        lockListeners = true;
        additiveBox.setChecked(module.getUserValue().additive);
        blendAddBox.setChecked(module.getUserValue().isBlendAdd);
        attachedBox.setChecked(module.getUserValue().attached);
        continuousBox.setChecked(module.getUserValue().continuous);
        alignedBox.setChecked(module.getUserValue().aligned);
        immortalBox.setChecked(module.getUserValue().immortal);
        youngestInBackBox.setChecked(module.getUserValue().youngestInBack);
        lockListeners = false;
    }

    @Override
    protected float reportPrefWidth() {
        return 170;
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        fromDataToUI();
    }

    public void setAttached(boolean attached) {
        attachedBox.setChecked(attached);
    }

    public void setContinuous(boolean attached) {
        continuousBox.setChecked(attached);
    }

    public void setBlendAdd(boolean blendAdd) {
        blendAddBox.setChecked(blendAdd);
    }

    public void setAdditive(boolean attached) {
        additiveBox.setChecked(attached);
    }

    public void setAligned(boolean attached) {
        alignedBox.setChecked(attached);
    }

    public void setImmortal(boolean immortal) {
        immortalBox.setChecked(immortal);
    }
}
