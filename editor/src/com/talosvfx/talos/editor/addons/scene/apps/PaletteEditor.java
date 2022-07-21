package com.talosvfx.talos.editor.addons.scene.apps;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.PaletteMetadata;

public class PaletteEditor extends AEditorApp {

    @Override
    protected void initContent() {
        Table content = new Table();

        add(content).size(480, 480);
    }

    @Override
    protected String getTitle() {
        return "Palette Editor";
    }

    public AEditorApp show(PaletteMetadata metadata) {
        // do some custom things

        return super.show();
    }
}
