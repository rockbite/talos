package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.*;

import java.util.function.Supplier;

public class SpriteRendererComponent extends RendererComponent {

    public TextureRegion texture;

    public String path = "";

    public Color color = new Color(Color.WHITE);
    public boolean flipX;
    public boolean flipY;
    public RenderMode renderMode = RenderMode.simple;

    public enum RenderMode {
        simple,
        sliced,
        tiled
    }

    @Override
    public Class<? extends IPropertyProvider> getType() {
        return getClass();
    }

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        AssetSelectWidget textureWidget = new AssetSelectWidget("Texture", "png", new Supplier<String>() {
            @Override
            public String get() {
                return path;
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report(String value) {
                path = value;
                reloadTexture();
            }
        });

        PropertyWidget colorWidget = WidgetFactory.generate(this, "color", "Color");
        PropertyWidget flipXWidget = WidgetFactory.generate(this, "flipX", "Flip X");
        PropertyWidget flipYWidget = WidgetFactory.generate(this, "flipY", "Flip Y");
        PropertyWidget renderModesWidget = WidgetFactory.generate(this, "renderMode", "Render Mode");

        properties.add(textureWidget);
        properties.add(colorWidget);
        properties.add(flipXWidget);
        properties.add(flipYWidget);
        properties.add(renderModesWidget);

        Array<PropertyWidget> superList = super.getListOfProperties();
        properties.addAll(superList);

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
        FileHandle file = AssetImporter.get(path);
        if(path.length() > 0 && file.exists()) {
            try {
                texture = new TextureRegion(new Texture(file));
            } catch (Exception e) {
                texture = new TextureRegion(new Texture(Gdx.files.internal("white.png")));
            }
        } else {
            texture = new TextureRegion(new Texture(Gdx.files.internal("white.png")));
        }
    }

    @Override
    public void write (Json json) {
        json.writeValue("path", path);

        json.writeValue("color", color);
        json.writeValue("flipX", flipX);
        json.writeValue("flipY", flipY);
        json.writeValue("renderMode", renderMode);

        super.write(json);

    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        path = jsonData.getString("path");
        reloadTexture();

        color = json.readValue(Color.class, jsonData.get("color"));
        if(color == null) color = new Color(Color.WHITE);

        flipX = jsonData.getBoolean("flipX", false);
        flipY = jsonData.getBoolean("flipY", false);
        renderMode = json.readValue(RenderMode.class, jsonData.get("renderMode"));
        if(renderMode == null) renderMode = RenderMode.simple;

        super.read(json, jsonData);
    }

    public TextureRegion getTexture () {
        if(texture == null) {
            reloadTexture();
        }
        return texture;
    }

    @Override
    public boolean notifyAssetPathChanged (String oldPath, String newPath) {
        if(path.equals(oldPath)) {
            path = newPath;
            reloadTexture();
            return true;
        }

        return false;
    }
}
