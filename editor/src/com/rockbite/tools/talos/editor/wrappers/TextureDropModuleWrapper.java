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

import java.io.File;

public abstract class TextureDropModuleWrapper<T extends Module> extends ModuleWrapper<T> {

    protected TextureDropWidget<Module> dropWidget;
    protected TextureRegion defaultRegion;

    protected String filePath;
    protected String fileName;

    boolean isDefaultSet = false; //TODO: this is a hack for loading

    @Override
    protected void configureSlots() {
        defaultRegion = new TextureRegion(new Texture(Gdx.files.internal("fire.png")));
        dropWidget = new TextureDropWidget<Module>(defaultRegion, getSkin());
    }

    @Override
    public void setModule(T module) {
        super.setModule(module);
        if(!isDefaultSet) {
            setModuleRegion("fire", defaultRegion);
            isDefaultSet = true;
        }
    }

    public abstract void setModuleRegion(String name, TextureRegion region);

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
                setModuleRegion(fileHandle.nameWithoutExtension(), textureRegion);
                dropWidget.setDrawable(new TextureRegionDrawable(textureRegion));

                filePath = paths[0]+"";
                fileName = fileHandle.name();
            }
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);

        fileName = jsonData.getString("fileName", null);
        filePath = jsonData.getString("filePath", null);
        if (filePath != null) {
            final TextureRegion region = TalosMain.Instance().Project().getProjectAssetProvider().findRegion(fileName);
            if (region != null) {
                dropWidget.setDrawable(new TextureRegionDrawable(region));
                setModuleRegion(fileName, region);
            } else {
                FileHandle fileHandle = tryAndFindTexture(filePath);

                final Texture texture = new Texture(fileHandle);
                TalosMain.Instance().Project().getProjectAssetProvider().addTextureAsTextureRegion(fileHandle.nameWithoutExtension(), texture);
                final TextureRegion textureRegion = new TextureRegion(texture);
                setModuleRegion(fileHandle.nameWithoutExtension(), textureRegion);
                dropWidget.setDrawable(new TextureRegionDrawable(textureRegion));

                filePath = fileHandle.path();
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

    private FileHandle tryAndFindTexture(String path) {
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
        FileHandle fileHandle = tryAndFindTexture(path);
        if(fileHandle.exists()) {
            TextureRegion region = new TextureRegion(new Texture(fileHandle));
            setModuleRegion(fileHandle.nameWithoutExtension(), region);
            dropWidget.setDrawable(new TextureRegionDrawable(region));
        }
        filePath = path+"";
        fileName = fileHandle.name();
    }
}
