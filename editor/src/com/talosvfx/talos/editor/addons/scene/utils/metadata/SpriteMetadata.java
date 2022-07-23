package com.talosvfx.talos.editor.addons.scene.utils.metadata;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorProject;
import com.talosvfx.talos.editor.addons.scene.apps.AEditorApp;
import com.talosvfx.talos.editor.addons.scene.apps.spriteeditor.SpriteEditor;
import com.talosvfx.talos.editor.addons.scene.events.PropertyHolderEdited;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ButtonPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;

public class SpriteMetadata extends AMetadata {

    public int[] borderData = {0, 0, 0, 0};

    public float pixelsPerUnit = 100;

    public Texture.TextureFilter filterMode = Texture.TextureFilter.Nearest;

    public SpriteMetadata() {
        super();
    }

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> propertyWidgets = new Array<>();

        propertyWidgets.add(WidgetFactory.generate(this, "pixelsPerUnit", "pxToWorld"));
        propertyWidgets.add(WidgetFactory.generate(this, "filterMode", "Filter"));

        ButtonPropertyWidget<String> spriteEditor = new ButtonPropertyWidget<String>("Sprite Editor", new ButtonPropertyWidget.ButtonListener<String>() {
            @Override
            public void clicked (ButtonPropertyWidget<String> widget) {
                SpriteEditor spriteEditor = new SpriteEditor(SpriteMetadata.this);
                spriteEditor.setListener(new SpriteEditor.SpriteMetadataListener() {
                    @Override
                    public void changed(int left, int right, int top, int bottom) {
                        borderData[0] = left;
                        borderData[1] = right;
                        borderData[2] = top;
                        borderData[3] = bottom;

                        Notifications.fireEvent(Notifications.obtainEvent(PropertyHolderEdited.class));
                    }
                });
                SceneEditorAddon.get().openApp(spriteEditor, AEditorApp.AppOpenStrategy.WINDOW);
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
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        JsonValue borderDataJsonValue = jsonData.get("borderData");
        if (borderDataJsonValue != null) {
            borderData = json.readValue(int[].class, borderDataJsonValue);
        }
        pixelsPerUnit = jsonData.getFloat("pixelsPerUnit", 100);
    }

    public boolean isSlice () {
        for (int i = 0; i < 4; i++) {
            boolean isNonZeroBorderData = borderData[i] != 0;
            if (isNonZeroBorderData) return true;
        }
        return false;
    }
}
