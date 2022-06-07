package com.talosvfx.talos.editor.addons.scene.apps;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;

public class SpriteEditor extends AEditorApp {
    private SpriteMetadataListener listener;
    private EditPanel editPanel;
    private Table ninePatchPreview;
    private ShapeRenderer shapeRenderer;
    private NinePatchDrawable patchDrawable;
    private Texture texture;

    @Override
    protected void initContent() {
        shapeRenderer = new ShapeRenderer();

        Table content = new Table();
        editPanel = new EditPanel(shapeRenderer, new EditPanel.EditPanelListener() {
            @Override
            public void changed(float left, float right, float top, float bottom) {
                NinePatch patch = new NinePatch(
                    texture,
                    (int) left,
                    (int) right,
                    (int) top,
                    (int) bottom
                );
                patchDrawable.setPatch(patch);
            }
        });
        Table rightSide = new Table();
        ninePatchPreview = new Table();

        TextButton saveSpriteMetaData = new TextButton("Save", getSkin());
        saveSpriteMetaData.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                saveAndClose();
            }
        });

        // To do: make lines
        rightSide.add(ninePatchPreview).size(175, 175).space(20).right();
        rightSide.row();
        rightSide.add(saveSpriteMetaData).expand().size(60, 40).bottom().right();

        content.pad(15);
        content.add(editPanel).size(370).space(40);
        content.add(rightSide).size(300, 370);
        add(content).size(740, 400);
    }

    private void saveAndClose() {
        if (listener != null) {
            listener.changed(
                    (int) editPanel.getLeft(),
                    (int) editPanel.getRight(),
                    (int) editPanel.getTop(),
                    (int) editPanel.getBottom()
            );
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
        super.show();
        this.listener = listener;

        // get ninepatch
        FileHandle file = AssetImporter.getFileFromMetadataHandle(metadata.currentFile);
        texture = new Texture(file);
        NinePatch patch = new NinePatch(
            texture,
            metadata.borderData[0],
            metadata.borderData[1],
            metadata.borderData[2],
            metadata.borderData[3]
        );
        patchDrawable = new NinePatchDrawable(patch);
        // live
        editPanel.show(metadata, texture);

        // preview
        ninePatchPreview.clear();
        Image vertical = new Image(patchDrawable);
        Image square = new Image(patchDrawable);
        Image horizontal = new Image(patchDrawable);
        ninePatchPreview.add(vertical).growY().space(20);
        ninePatchPreview.add(square).grow().space(20);
        ninePatchPreview.row();
        ninePatchPreview.add(horizontal).growX().colspan(2).space(20);
        return this;
    }

    @Override
    public void hide() {
        listener = null;
        TalosMain.Instance().UIStage().getStage().setScrollFocus(null);
        super.hide();
    }
}
