package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.dialogs.SettingsDialog;
import com.rockbite.tools.talos.editor.widgets.IntegerInputWidget;
import com.rockbite.tools.talos.editor.widgets.TextureDropWidget;
import com.rockbite.tools.talos.runtime.Slot;
import com.rockbite.tools.talos.runtime.modules.*;

import java.io.File;

public class PolylineModuleWrapper extends ModuleWrapper<PolylineModule> {

    TextureDropWidget dropWidget;
    TextureRegion defaultRegion;

    String filePath;
    String fileName;
    boolean isDefaultSet = false; //TODO: this is a hack for loading

    private IntegerInputWidget interpolationPoints;

    @Override
    public void setModule(PolylineModule module) {
        super.setModule(module);
        if(!isDefaultSet) {
            module.setRegion("fire", defaultRegion);
            isDefaultSet = true;
        }
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

        dropWidget = new TextureDropWidget(defaultRegion, getSkin());
        rightWrapper.add(dropWidget).size(50).right().row();

        rightWrapper.add().growY().row();
        addOutputSlot("output", PolylineModule.OUTPUT);
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
    protected float reportPrefWidth() {
        return 180;
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        interpolationPoints.setValue(module.pointCount - 2);

        fileName = jsonData.getString("fileName", null);
        filePath = jsonData.getString("filePath", null);
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

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("fileName", fileName);
        json.writeValue("filePath", filePath);
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

    @Override
    public Class<? extends Module>  getSlotsPreferredModule(Slot slot) {

        if(slot.getIndex() == PolylineModule.OFFSET) return NoiseModule.class;
        if(slot.getIndex() == PolylineModule.THICKNESS) return CurveModule.class;
        if(slot.getIndex() == PolylineModule.COLOR) return GradientColorModule.class;
        if(slot.getIndex() == PolylineModule.TRANSPARENCY) return CurveModule.class;

        if(slot.getIndex() == PolylineModule.LEFT_TANGENT) return Vector2Module.class;
        if(slot.getIndex() == PolylineModule.RIGHT_TANGENT) return Vector2Module.class;

        return null;
    }

}
