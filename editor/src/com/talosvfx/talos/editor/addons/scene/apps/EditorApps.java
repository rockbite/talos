package com.talosvfx.talos.editor.addons.scene.apps;

import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;

public class EditorApps {

    private static EditorApps instance;

    private SpriteEditor spriteEditor;

    private EditorApps() {
        spriteEditor = new SpriteEditor();
    }

    public static EditorApps getInstance() {
        if(instance == null) {
            instance = new EditorApps();
        }

        return instance;
    }

    public void openSceneEditor(SpriteMetadata metadata, SpriteEditor.SpriteMetadataListener listener) {
        spriteEditor.show(metadata, listener);
    }
}
