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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.dialogs.SettingsDialog;
import com.talosvfx.talos.editor.widgets.IntegerInputWidget;
import com.talosvfx.talos.editor.widgets.TextureDropWidget;
import com.talosvfx.talos.runtime.vfx.Slot;

import com.talosvfx.talos.runtime.vfx.modules.AbstractModule;
import com.talosvfx.talos.runtime.vfx.modules.CurveModule;
import com.talosvfx.talos.runtime.vfx.modules.GradientColorModule;
import com.talosvfx.talos.runtime.vfx.modules.NoiseModule;
import com.talosvfx.talos.runtime.vfx.modules.PolylineModule;
import com.talosvfx.talos.runtime.vfx.modules.Vector2Module;

import java.io.File;

public class PolylineModuleWrapper extends TextureDropModuleWrapper<PolylineModule> {

    private IntegerInputWidget interpolationPoints;

    @Override
    public void setModuleToDefaults () {
        module.regionName = "fire";
    }

    @Override
    public void setModuleRegion (String name, Sprite region) {
        module.setRegion(name, region);
    }

    @Override
    protected void configureSlots() {
        defaultRegion = new TextureRegion(new Texture(Gdx.files.internal("fire.png")));

        addInputSlot("offset",  PolylineModule.OFFSET);
        addInputSlot("thickness",  PolylineModule.THICKNESS);
        addInputSlot("color",  PolylineModule.COLOR);
        addInputSlot("transparency",  PolylineModule.TRANSPARENCY);

        addInputSlot("left tangent",  PolylineModule.LEFT_TANGENT);
        addInputSlot("right tangent",  PolylineModule.RIGHT_TANGENT);

        interpolationPoints = new IntegerInputWidget("interpolation points", getSkin());
        interpolationPoints.setValue(0);
        leftWrapper.add(interpolationPoints).left().expandX();

        interpolationPoints.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setInterpolationPoints(interpolationPoints.getValue());
            }
        });

        dropWidget = new TextureDropWidget<AbstractModule>(defaultRegion, getSkin());
        rightWrapper.add(dropWidget).size(50).right().row();

        rightWrapper.add().growY().row();
        addOutputSlot("output", PolylineModule.OUTPUT);
    }

    @Override
    protected float reportPrefWidth() {
        return 180;
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        interpolationPoints.setValue(module.pointCount - 2);
    }

    @Override
    public void write (Json json) {
        super.write(json);
    }

    private FileHandle tryAndFineTexture(String path) {
        FileHandle fileHandle = Gdx.files.absolute(path);
        String fileName = fileHandle.name();
        if(!fileHandle.exists()) {
            if(TalosMain.Instance().ProjectController().getCurrentProjectPath() != null) {
                FileHandle parent = Gdx.files.absolute(TalosMain.Instance().ProjectController().getCurrentProjectPath()).parent();
                fileHandle = Gdx.files.absolute(parent.path() + "/" + fileName);
            }

            if(!fileHandle.exists()) {
                fileHandle = Gdx.files.absolute(TalosMain.Instance().Prefs().getString(SettingsDialog.ASSET_PATH) + File.separator + fileName);
            }
        }

        return fileHandle;
    }

    public void setTexture(String path) {
        FileHandle fileHandle = tryAndFineTexture(path);
        if(fileHandle.exists()) {
            TextureRegion region = new TextureRegion(new Texture(fileHandle));
            module.setRegion(fileHandle.nameWithoutExtension(), region);
            dropWidget.setDrawable(new TextureRegionDrawable(region));
        }
        filePath = fileHandle.path();
        regionName = fileHandle.nameWithoutExtension();
    }

    @Override
    public Class<? extends AbstractModule>  getSlotsPreferredModule(Slot slot) {

        if(slot.getIndex() == PolylineModule.OFFSET) return NoiseModule.class;
        if(slot.getIndex() == PolylineModule.THICKNESS) return CurveModule.class;
        if(slot.getIndex() == PolylineModule.COLOR) return GradientColorModule.class;
        if(slot.getIndex() == PolylineModule.TRANSPARENCY) return CurveModule.class;

        if(slot.getIndex() == PolylineModule.LEFT_TANGENT) return Vector2Module.class;
        if(slot.getIndex() == PolylineModule.RIGHT_TANGENT) return Vector2Module.class;

        return null;
    }

}
