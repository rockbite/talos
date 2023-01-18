package com.talosvfx.talos.editor.wrappers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.project.FileTracker;
import com.talosvfx.talos.editor.assets.TalosAssetProvider;
import com.talosvfx.talos.editor.project2.TalosVFXUtils;
import com.talosvfx.talos.editor.widgets.ui.common.LabelWithZoom;
import com.talosvfx.talos.runtime.modules.VectorFieldModule;
import com.talosvfx.talos.runtime.utils.VectorField;

public class VectorFieldModuleWrapper extends ModuleWrapper<VectorFieldModule> {

    Label vectorFieldLabel;

    @Override
    protected void configureSlots () {
        addInputSlot("field position", VectorFieldModule.POSITION);
        addInputSlot("field size", VectorFieldModule.SIZE_SCALE);
        addInputSlot("field force", VectorFieldModule.FORCE_SCALE);

        addOutputSlot("velocity", VectorFieldModule.VELOCITY);
        addOutputSlot("angle", VectorFieldModule.ANGLE);

        leftWrapper.add().expandY().row();
        rightWrapper.add().expandY().row();

        vectorFieldLabel = new LabelWithZoom("drop .fga file here", getSkin());
        vectorFieldLabel.setAlignment(Align.center);
        vectorFieldLabel.setWrap(true);
        contentWrapper.add(vectorFieldLabel).padTop(60f).padBottom(10f).size(180, 50).center().expand();
    }

    @Override
    public void fileDrop(String[] paths, float x, float y) {
        if(paths.length > 0) {
            String path = paths[0];
            FileHandle handle = Gdx.files.absolute(path);
            VectorField vectorField = new VectorField();
            vectorField.setBakedData(handle);
            TalosAssetProvider assetProvider = TalosVFXUtils.talosAssetProvider;
            assetProvider.addVectorField(handle.nameWithoutExtension(), vectorField);
            setVectorField(vectorField, handle.nameWithoutExtension());

            TalosMain.Instance().FileTracker().trackFile(handle, new FileTracker.Tracker() {
                @Override
                public void updated(FileHandle handle) {
                    VectorField vectorField = new VectorField();
                    vectorField.setBakedData(handle);
                    TalosAssetProvider assetProvider = TalosVFXUtils.talosAssetProvider;
                    assetProvider.addVectorField(handle.nameWithoutExtension(), vectorField);
                    setVectorField(vectorField, handle.nameWithoutExtension());
                }
            });
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);

        if(module.fgaFileName != null && !module.fgaFileName.isEmpty()) {
            setVectorFieldLabel(module.fgaFileName);
        }
    }

    private void setVectorField(VectorField vectorField, String fileName) {
        module.setVectorField(vectorField, fileName);
        setVectorFieldLabel(fileName);
    }

    public void setVectorFieldLabel(String fileName) {
        vectorFieldLabel.setText("loaded " + fileName);
    }

    @Override
    protected float reportPrefWidth () {
        return 180;
    }
}
