package com.talosvfx.talos.editor.addons.scene.apps;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;

public class SpriteEditor extends AEditorApp {
    private SpriteMetadata metadata;
    private SpriteMetadataListener listener;

    @Override
    protected void initContent() {
        Table content = new Table();

        // todo make the content

        add(content).size(1000, 600);
    }

    private void saveAndClose() {
        if(listener != null) {
            listener.changed(0, 0, 0, 0);
        }
        hide();
    }

    @Override
    public String getTitle() {
        return "Sprite Editor";
    }

    public static interface SpriteMetadataListener {
        void changed(int left, int right, int top, int bottom);
    }

    public AEditorApp show(SpriteMetadata metadata, SpriteMetadataListener listener) {
        this.metadata = metadata;
        this.listener = listener;

        return super.show();
    }

    @Override
    public void hide() {
        listener = null;
        super.hide();
    }
}
