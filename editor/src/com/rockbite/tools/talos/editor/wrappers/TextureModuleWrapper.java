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

package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.dialogs.SettingsDialog;
import com.rockbite.tools.talos.editor.widgets.TextureDropWidget;
import com.rockbite.tools.talos.runtime.modules.Module;
import com.rockbite.tools.talos.runtime.modules.TextureModule;

import java.io.File;

public class TextureModuleWrapper extends ModuleWrapper<TextureModule> {

   TextureDropWidget<Module> dropWidget;

    TextureRegion defaultRegion;

    String filePath;
    String fileName;

    boolean isDefaultSet = false; //TODO: this is a hack for loading

    public TextureModuleWrapper() {
        super();
    }

    @Override
    protected float reportPrefWidth() {
        return 150;
    }

    @Override
    public void setModule(TextureModule module) {
        super.setModule(module);
        if(!isDefaultSet) {
            module.setRegion("fire", defaultRegion);
            isDefaultSet = true;
        }
    }

    @Override
    protected void configureSlots() {

        defaultRegion = new TextureRegion(new Texture(Gdx.files.internal("fire.png")));

        dropWidget = new TextureDropWidget<Module>(defaultRegion, getSkin());

        addOutputSlot("output", TextureModule.OUTPUT);

        contentWrapper.add(dropWidget).size(50).left().padLeft(10);
        contentWrapper.add().expandX();

    }

    @Override
    public void fileDrop(String[] paths, float x, float y) {
        if(paths.length == 1) {

            String resourcePath = paths[0];
            FileHandle fileHandle = Gdx.files.absolute(resourcePath);

            final String extension = fileHandle.extension();

            if (extension.endsWith("png") || extension.endsWith("jpg")) {
                final Texture texture = new Texture(fileHandle);
                TalosMain.Instance().Project().getProjectAssetProvider().addTextureAsTextureRegion(fileHandle.nameWithoutExtension(), texture);
                final TextureRegion textureRegion = new TextureRegion(texture);
                module.setRegion(fileHandle.nameWithoutExtension(), textureRegion);
                dropWidget.setDrawable(new TextureRegionDrawable(textureRegion));

                filePath = paths[0]+"";
                fileName = fileHandle.name();
            }
        }
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("fileName", fileName);
        json.writeValue("filePath", filePath);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        fileName = jsonData.getString("fileName");
        filePath = jsonData.getString("filePath");
        if (filePath != null) {
            final TextureRegion region = TalosMain.Instance().Project().getProjectAssetProvider().findRegion(fileName);
            if (region != null) {
                dropWidget.setDrawable(new TextureRegionDrawable(region));
                module.setRegion(fileName, region);
            } else {
                FileHandle fileHandle = tryAndFineTexture(filePath);

                final Texture texture = new Texture(fileHandle);
                TalosMain.Instance().Project().getProjectAssetProvider().addTextureAsTextureRegion(fileHandle.nameWithoutExtension(), texture);
                final TextureRegion textureRegion = new TextureRegion(texture);
                module.setRegion(fileHandle.nameWithoutExtension(), textureRegion);
                dropWidget.setDrawable(new TextureRegionDrawable(textureRegion));

                filePath = fileHandle.path();
                fileName = fileHandle.name();
            }
        }
    }

    private FileHandle tryAndFineTexture(String path) {
        FileHandle fileHandle = Gdx.files.absolute(path);
        String fileName = fileHandle.name();
        if(!fileHandle.exists()) {
            if(TalosMain.Instance().Project().getPath() != null) {
                FileHandle parent = Gdx.files.absolute(TalosMain.Instance().Project().getPath()).parent();
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
        filePath = path+"";
        fileName = fileHandle.name();
    }
}
