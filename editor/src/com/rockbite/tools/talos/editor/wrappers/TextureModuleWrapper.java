package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.dialogs.SettingsDialog;
import com.rockbite.tools.talos.runtime.modules.TextureModule;

import java.io.File;

public class TextureModuleWrapper extends ModuleWrapper<TextureModule> {

    Image image;

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
            module.setRegion(defaultRegion);
            module.fileName = fileName;
            isDefaultSet = true;
        }
    }

    @Override
    protected void configureSlots() {
        defaultRegion = new TextureRegion(new Texture(Gdx.files.internal("fire.png")));
        image = new Image(defaultRegion);

        addOutputSlot("output", TextureModule.OUTPUT);

        contentWrapper.add(image).size(50).left().padLeft(10);
        contentWrapper.add().expandX();

    }

    @Override
    public void fileDrop(String[] paths, float x, float y) {
        if(paths.length == 1) {
            FileHandle fileHandle = Gdx.files.absolute(paths[0]);
            TextureRegion region = new TextureRegion(new Texture(fileHandle));
            module.setRegion(region);
            image.setDrawable(new TextureRegionDrawable(region));
            filePath = paths[0]+"";
            fileName = fileHandle.name();
            module.fileName = fileName;
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
            FileHandle fileHandle = tryAndFineTexture(filePath);
            TextureRegion region = new TextureRegion(new Texture(fileHandle));
            module.setRegion(region);
            module.fileName = fileName;
            image.setDrawable(new TextureRegionDrawable(region));
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
            module.setRegion(region);
            image.setDrawable(new TextureRegionDrawable(region));
        }
        filePath = path+"";
        fileName = fileHandle.name();
        if(module != null) {
            module.fileName = fileName;
        }
    }
}
