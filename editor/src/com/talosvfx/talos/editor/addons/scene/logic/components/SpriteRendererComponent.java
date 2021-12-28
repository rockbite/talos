package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.widgets.propertyWidgets.FloatPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.LabelWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.Vector2PropertyWidget;

public class SpriteRendererComponent implements IComponent, Json.Serializable {

    public TextureRegion texture;

    public String path;

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        LabelWidget textureWidget = new LabelWidget("Texture") {
            @Override
            public String getValue () {
                FileHandle fileHandle = Gdx.files.absolute(path);
                return fileHandle.nameWithoutExtension();
            }
        };

        properties.add(textureWidget);

        return properties;
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Sprite Renderer";
    }

    @Override
    public int getPriority () {
        return 2;
    }

    public void reloadTexture () {
        texture = new TextureRegion(new Texture(Gdx.files.absolute(path))); //todo: maybe make this into atlas?
    }

    @Override
    public void write (Json json) {
        json.writeValue("path", path);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        path = jsonData.getString("path");
        reloadTexture();
    }
}
