package com.talosvfx.talos.editor.addons.scene.apps;

import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.PaletteMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;

public class EditorApps {

    private static EditorApps instance;

    private SpriteEditor spriteEditor;

    private PaletteEditor paletteEditor;

    private EditorApps() {
        spriteEditor = new SpriteEditor();
        paletteEditor = new PaletteEditor();
    }

    public static EditorApps getInstance() {
        if(instance == null) {
            instance = new EditorApps();
        }

        return instance;
    }

    public void openSpriteEditor(SpriteMetadata metadata, SpriteEditor.SpriteMetadataListener listener) {
        spriteEditor.show(metadata, listener);
    }

    public void openPaletteEditor() {
        paletteEditor.show();
    }
}
