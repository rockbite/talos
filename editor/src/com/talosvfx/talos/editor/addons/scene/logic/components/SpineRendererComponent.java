package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;

import java.util.function.Supplier;

public class SpineRendererComponent extends RendererComponent {

    public String path = "";
    public TextureAtlas textureAtlas;

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        AssetSelectWidget atlasWidget = new AssetSelectWidget("Skeleton", GameAssetType.SKELETON, new Supplier<String>() {
            @Override
            public String get() {
                return path;
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report(String value) {
                path = value;
                reloadAtlas();
            }
        });

        properties.add(atlasWidget);

        Array<PropertyWidget> superList = super.getListOfProperties();
        properties.addAll(superList);

        return properties;
    }

    public void reloadAtlas () {
        if(path == null) return;
        FileHandle handle = AssetImporter.get(path);
        if(handle.exists()) {
            textureAtlas = new TextureAtlas(handle);
        }
    }


    @Override
    public String getPropertyBoxTitle () {
        return "Spine Renderer";
    }

    @Override
    public int getPriority () {
        return 3;
    }

    @Override
    public Class<? extends IPropertyProvider> getType () {
        return getClass();
    }

    @Override
    public void write (Json json) {
        json.writeValue("path", path);
        super.write(json);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        path = jsonData.getString("path");
        reloadAtlas();
    }


}
