package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.*;

import java.util.function.Supplier;

public class SpriteRendererComponent extends RendererComponent {

    public GameAsset<Texture> texture;
    TextureRegion textureRegion;

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

        AssetSelectWidget textureWidget = new AssetSelectWidget("Texture", GameAssetType.SPRITE, new Supplier<String>() {
            @Override
            public String get() {
                return path;
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report(String value) {
                path = value;
                loadTexture();
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

    GameAsset.GameAssetUpdateListener gameAssetUpdateListener = new GameAsset.GameAssetUpdateListener() {
        @Override
        public void onUpdate () {
            if (texture.isBroken()) {
                textureRegion = AssetRepository.getInstance().brokenTextureRegion;
            } else {
                textureRegion = new TextureRegion(texture.getResource());
            }
        }
    };

    public void loadTexture () {
        FileHandle file = AssetImporter.get(path);

        if (texture != null) {
            //Remove from old game asset, it might be the same, but it may also have changed
            texture.listeners.removeValue(gameAssetUpdateListener, true);
        }

        texture = AssetRepository.getInstance().getAssetForPath(file, Texture.class);
        gameAssetUpdateListener.onUpdate();

        texture.listeners.add(gameAssetUpdateListener);
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

        loadTexture();

        color = json.readValue(Color.class, jsonData.get("color"));
        if(color == null) color = new Color(Color.WHITE);

        flipX = jsonData.getBoolean("flipX", false);
        flipY = jsonData.getBoolean("flipY", false);
        renderMode = json.readValue(RenderMode.class, jsonData.get("renderMode"));
        if(renderMode == null) renderMode = RenderMode.simple;

        super.read(json, jsonData);
    }

    public TextureRegion getTextureRegion () {
        return textureRegion;
    }

    @Override
    public boolean notifyAssetPathChanged (String oldPath, String newPath) {
        if(path.equals(oldPath)) {
            path = newPath;
            return true;
        }

        return false;
    }
}
