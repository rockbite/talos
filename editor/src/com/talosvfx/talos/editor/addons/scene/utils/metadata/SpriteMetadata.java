package com.talosvfx.talos.editor.addons.scene.utils.metadata;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.apps.spriteeditor.SpriteEditorApp;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.events.SpritePixelPerUnitUpdateEvent;
import com.talosvfx.talos.runtime.assets.AMetadata;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ButtonPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpriteMetadata extends AMetadata {

    private static final Logger logger = LoggerFactory.getLogger(SpriteMetadata.class);

    public int[] borderData = {0, 0, 0, 0};

    public float pixelsPerUnit = DefaultConstants.PIXELS_PER_UNIT;

    public Texture.TextureFilter minFilter = Texture.TextureFilter.Nearest;
    public Texture.TextureFilter magFilter = Texture.TextureFilter.Nearest;

    public SpriteMetadata() {
        super();
    }

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> propertyWidgets = new Array<>();

        final PropertyWidget pixelToWorldPropertyWidget = WidgetFactory.generate(this, "pixelsPerUnit", "pxToWorld");
        pixelToWorldPropertyWidget.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                Notifications.fireEvent(Notifications.obtainEvent(SpritePixelPerUnitUpdateEvent.class).setSpriteMetadata(SpriteMetadata.this));
            }
        });
        propertyWidgets.add(pixelToWorldPropertyWidget);
        propertyWidgets.add(WidgetFactory.generate(this, "minFilter", "MinFilter"));
        propertyWidgets.add(WidgetFactory.generate(this, "magFilter", "MagFilter"));

        ButtonPropertyWidget<String> spriteEditor = new ButtonPropertyWidget<String>("Sprite Editor", new ButtonPropertyWidget.ButtonListener<String>() {
            @Override
            public void clicked (ButtonPropertyWidget<String> widget) {
                logger.info("todo open sprite editor request");
                GameAsset<Texture> assetForPath = (GameAsset<Texture>) AssetRepository.getInstance().getAssetForPath(link.handle, false);
                SharedResources.appManager.openApp(assetForPath, SpriteEditorApp.class);
            }
        });
        propertyWidgets.add(spriteEditor);

        return propertyWidgets;
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Sprite";
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("borderData", borderData);
        json.writeValue("pixelsPerUnit", pixelsPerUnit);
        json.writeValue("minFilter", minFilter);
        json.writeValue("minFilter", magFilter);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        JsonValue borderDataJsonValue = jsonData.get("borderData");
        if (borderDataJsonValue != null) {
            borderData = json.readValue(int[].class, borderDataJsonValue);
        }
        pixelsPerUnit = jsonData.getFloat("pixelsPerUnit", 100);
        minFilter = json.readValue("minFilter", Texture.TextureFilter.class, Texture.TextureFilter.Nearest, jsonData);
        magFilter = json.readValue("magFilter", Texture.TextureFilter.class, Texture.TextureFilter.Nearest, jsonData);
    }

    public boolean isSlice () {
        for (int i = 0; i < 4; i++) {
            boolean isNonZeroBorderData = borderData[i] != 0;
            if (isNonZeroBorderData) return true;
        }
        return false;
    }
}
