package com.talosvfx.talos.editor.addons.scene.apps;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class PaletteEditor extends AEditorApp {

    public PaletteEditor(Object object) {
        super(object);
    }

    @Override
    public void initContent() {
        content = new Table();
    }

    @Override
    public String getTitle() {
        return "Palette Editor";
    }

}
