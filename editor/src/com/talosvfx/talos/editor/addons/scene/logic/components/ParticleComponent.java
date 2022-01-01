package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;

public class ParticleComponent extends RendererComponent {

    public ParticleEffectDescriptor descriptor;
    public String path = "";

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        AssetSelectWidget descriptorWidget = new AssetSelectWidget("Effect", "p") {
            @Override
            public String getValue () {
                FileHandle fileHandle = Gdx.files.absolute(path);
                return fileHandle.path();
            }

            @Override
            public void valueChanged (String value) {
                path = value;
                reloadDescriptor();
            }
        };

        properties.add(descriptorWidget);
        properties.addAll(super.getListOfProperties());

        return properties;
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Particle Effect";
    }

    @Override
    public int getPriority () {
        return 2;
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
    }

    @Override
    public void write (Json json) {
        super.write(json);
    }

    public void reloadDescriptor () {
        FileHandle handle = Gdx.files.absolute(path);
        if(handle.exists() && handle.extension().equals("p")) {
            try {
                descriptor = new ParticleEffectDescriptor();
                descriptor.setAssetProvider(SceneEditorAddon.get().assetProvider);
                descriptor.load(handle);
            } catch (Exception e) {

            }
        } else {
            // load default
            descriptor = new ParticleEffectDescriptor();
            descriptor.setAssetProvider(SceneEditorAddon.get().assetProvider);
            descriptor.load(Gdx.files.internal("addons/scene/missing/sample.p"));
        }
    }
}
