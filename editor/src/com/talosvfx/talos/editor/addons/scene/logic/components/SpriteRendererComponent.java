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
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.*;

public class SpriteRendererComponent implements RendererComponent, Json.Serializable {

    public TextureRegion texture;

    public String path = "";

    public Color color = new Color(Color.WHITE);
    public boolean flipX;
    public boolean flipY;
    public RenderMode renderMode = RenderMode.simple;
    public String sortingLayer = "Default";
    public int orderingInLayer;

    @Override
    public String getSortingLayer () {
        return sortingLayer;
    }

    @Override
    public void setSortingLayer (String name) {
        sortingLayer = name;
    }

    public enum RenderMode {
        simple,
        sliced,
        tiled
    }

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        AssetSelectWidget textureWidget = new AssetSelectWidget("Texture") {
            @Override
            public String getValue () {
                FileHandle fileHandle = Gdx.files.absolute(path);
                return fileHandle.path();
            }

            @Override
            public void valueChanged (String value) {
                path = value;
                reloadTexture();
            }
        };

        PropertyWidget colorWidget = WidgetFactory.generate(this, "color", "Color");
        PropertyWidget flipXWidget = WidgetFactory.generate(this, "flipX", "Flip X");
        PropertyWidget flipYWidget = WidgetFactory.generate(this, "flipY", "Flip Y");
        PropertyWidget renderModesWidget = WidgetFactory.generate(this, "renderMode", "Render Mode");
        PropertyWidget orderingInLayerWidget = WidgetFactory.generate(this, "orderingInLayer", "Ordering");

        SelectBoxWidget layerWidget = new SelectBoxWidget("Sorting Layer") {
            @Override
            public Array<String> getOptionsList () {
                return SceneEditorAddon.get().workspace.getLayerList();
            }

            @Override
            public String getValue () {
                return sortingLayer;
            }

            @Override
            public void valueChanged (String value) {
                sortingLayer = value;
            }
        };


        properties.add(textureWidget);
        properties.add(colorWidget);
        properties.add(flipXWidget);
        properties.add(flipYWidget);
        properties.add(renderModesWidget);
        properties.add(layerWidget);
        properties.add(orderingInLayerWidget);

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
        FileHandle file = Gdx.files.absolute(path);
        if(file.exists()) {
            try {
                texture = new TextureRegion(new Texture(file));
            } catch (Exception e) {
                texture = TalosMain.Instance().getSkin().getRegion("white");
            }
        } else {
            texture = TalosMain.Instance().getSkin().getRegion("white");
        }
    }

    @Override
    public void write (Json json) {
        json.writeValue("path", path);

        json.writeValue("color", color);
        json.writeValue("flipX", flipX);
        json.writeValue("flipY", flipY);
        json.writeValue("renderMode", renderMode);
        json.writeValue("sortingLayer", sortingLayer);
        json.writeValue("orderingInLayer", orderingInLayer);

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
        sortingLayer = jsonData.getString("sortingLayer", "Default");
        orderingInLayer = jsonData.getInt("orderingInLayer", 0);
    }

    public TextureRegion getTexture () {
        if(texture == null) {
            reloadTexture();
        }
        return texture;
    }
}
