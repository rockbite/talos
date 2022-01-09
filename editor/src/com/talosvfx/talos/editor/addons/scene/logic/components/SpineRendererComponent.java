package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
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

        AssetSelectWidget atlasWidget = new AssetSelectWidget("Atlas", "atlas", new Supplier<String>() {
            @Override
            public String get() {
                FileHandle fileHandle = Gdx.files.absolute(path);
                return fileHandle.path();
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
        if(path != null && Gdx.files.absolute(path).exists()) {
            textureAtlas = new TextureAtlas(Gdx.files.absolute(path));
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
