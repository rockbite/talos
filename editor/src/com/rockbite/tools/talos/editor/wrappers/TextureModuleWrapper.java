package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.modules.TextureModule;
public class TextureModuleWrapper extends ModuleWrapper<TextureModule> {

    Image image;

    TextureRegion defaultRegion;

    String filePath;
    String fileName;

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
        module.setRegion(defaultRegion);
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
            FileHandle fileHandle = Gdx.files.absolute(filePath);
            TextureRegion region = new TextureRegion(new Texture(fileHandle));
            module.setRegion(region);
            image.setDrawable(new TextureRegionDrawable(region));
        }
    }

    public void setTexture(String path) {
        FileHandle fileHandle = Gdx.files.absolute(path);
        TextureRegion region = new TextureRegion(new Texture(fileHandle));
        module.setRegion(region);
        image.setDrawable(new TextureRegionDrawable(region));
        filePath = path+"";
        fileName = fileHandle.name();
    }
}
