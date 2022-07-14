package com.talosvfx.talos.editor.addons.scene.utils.metadata;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorProject;
import com.talosvfx.talos.editor.addons.scene.apps.SpriteEditor;
import com.talosvfx.talos.editor.addons.scene.events.PropertyHolderEdited;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
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
                SceneEditorAddon sceneEditorAddon = ((SceneEditorProject) TalosMain.Instance().ProjectController().getProject()).sceneEditorAddon;
                sceneEditorAddon.Apps().openSceneEditor(SpriteMetadata.this, new SpriteEditor.SpriteMetadataListener() {
                    @Override
                    public void changed(int left, int right, int top, int bottom) {
                        borderData[0] = left;
                        borderData[1] = right;
                        borderData[2] = top;
                        borderData[3] = bottom;

                        Notifications.fireEvent(Notifications.obtainEvent(PropertyHolderEdited.class));
                    }
                });
            }
        });
        propertyWidgets.add(spriteEditor);

        return propertyWidgets;
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Sprite";
    }
}
