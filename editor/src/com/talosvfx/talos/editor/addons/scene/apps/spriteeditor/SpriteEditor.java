package com.talosvfx.talos.editor.addons.scene.apps.spriteeditor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.apps.AEditorApp;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;

public class SpriteEditor extends AEditorApp<SpriteMetadata> {

    private SpriteMetadataListener listener;

    private EditPanel editPanel;
    private NinePatchPreview ninePatchPreview;
    private NumberPanel leftProperty;
    private NumberPanel rightProperty;
    private NumberPanel topProperty;
    private NumberPanel bottomProperty;

    private Texture texture;
    private NinePatchDrawable patchDrawable;

    public SpriteEditor(SpriteMetadata metadata) {
        super(metadata);
        identifier = object.uuid  + "";
        initContent();
        show(metadata);
    }

    @Override
    public void initContent() {
        content = new Table();
        editPanel = new EditPanel(new EditPanel.EditPanelListener() {
            @Override
            public void changed(float left, float right, float top, float bottom) {
                leftProperty.setValue(left);
                rightProperty.setValue(right);
                topProperty.setValue(top);
                bottomProperty.setValue(bottom);
            }

            @Override
            public void dragStop(float left, float right, float top, float bottom) {
                updatePreview();
            }
        });
        Table rightSide = new Table();
        ninePatchPreview = new NinePatchPreview();
        Table numberControls = new Table();

        Label leftLabel = new Label("Left: ", TalosMain.Instance().getSkin());
        leftProperty = new NumberPanel();
        Label rightLabel = new Label("Right: ", TalosMain.Instance().getSkin());
        rightProperty = new NumberPanel();
        Label topLabel = new Label("Top: ", TalosMain.Instance().getSkin());
        topProperty = new NumberPanel();
        Label bottomLabel = new Label("Bottom: ", TalosMain.Instance().getSkin());
        bottomProperty = new NumberPanel();

        TextButton saveSpriteMetaData = new TextButton("Save", TalosMain.Instance().getSkin());
        saveSpriteMetaData.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                saveAndClose();
            }
        });

        rightSide.add(ninePatchPreview).size(NinePatchPreview.WIDTH, NinePatchPreview.HEIGHT).space(20).right();
        rightSide.row();
        rightSide.add(numberControls).expand().right();

        numberControls.add(leftLabel).right();
        numberControls.add(leftProperty).growX().maxWidth(200).right().space(5);
        numberControls.row();

        numberControls.add(rightLabel).right();
        numberControls.add(rightProperty).growX().maxWidth(200).right().space(5);
        numberControls.row();

        numberControls.add(topLabel).right();
        numberControls.add(topProperty).growX().maxWidth(200).right().space(5);
        numberControls.row();

        numberControls.add(bottomLabel).right();
        numberControls.add(bottomProperty).growX().maxWidth(200).right().space(5);

        rightSide.row();
        rightSide.add(saveSpriteMetaData).size(60, 40).bottom().right().padTop(10);

        content.pad(15);
        content.add(editPanel).width(EditPanel.WIDTH).height(EditPanel.HEIGHT).space(40);
        content.add(rightSide).size(300, 370);

        leftProperty.setListener(new NumberPanel.NumberPanelListener() {
            @Override
            public void typed(float before, float after) {
                float left = leftProperty.getValue();
                float right = rightProperty.getValue();
                if (left >= texture.getWidth() - right) {
                    left = texture.getWidth() - right;
                }
                leftProperty.setValue(left);
                editPanel.set(EditPanel.LEFT, left);
                updatePreview();
            }

            @Override
            public void dragged(float before, float after) {
                float left = leftProperty.getValue();
                float right = rightProperty.getValue();
                if (left >= texture.getWidth() - right) {
                    left = before;
                }
                leftProperty.setValue(left);
                editPanel.set(EditPanel.LEFT, left);
            }

            @Override
            public void dragStop() {
                updatePreview();
            }
        });
        rightProperty.setListener(new NumberPanel.NumberPanelListener() {
            @Override
            public void typed(float before, float after) {
                float left = leftProperty.getValue();
                float right = rightProperty.getValue();
                if (right >= texture.getWidth() - left) {
                    right = texture.getWidth() - left;
                }
                rightProperty.setValue(right);
                editPanel.set(EditPanel.RIGHT, right);
                updatePreview();
            }

            @Override
            public void dragged(float before, float after) {
                float left = leftProperty.getValue();
                float right = rightProperty.getValue();
                if (right >= texture.getWidth() - left) {
                    right = before;
                }
                rightProperty.setValue(right);
                editPanel.set(EditPanel.RIGHT, right);
            }

            @Override
            public void dragStop() {
                updatePreview();
            }
        });
        topProperty.setListener(new NumberPanel.NumberPanelListener() {
            @Override
            public void typed(float before, float after) {
                float top = topProperty.getValue();
                float bottom = bottomProperty.getValue();
                if (top >= texture.getHeight() - bottom) {
                    top = texture.getHeight() - bottom;
                }
                topProperty.setValue(top);
                editPanel.set(EditPanel.TOP, top);
                updatePreview();
            }

            @Override
            public void dragged(float before, float after) {
                float top = topProperty.getValue();
                float bottom = bottomProperty.getValue();
                if (top >= texture.getHeight() - bottom) {
                    top = before;
                }
                topProperty.setValue(top);
                editPanel.set(EditPanel.TOP, top);
            }

            @Override
            public void dragStop() {
                updatePreview();
            }
        });
        bottomProperty.setListener(new NumberPanel.NumberPanelListener() {
            @Override
            public void typed(float before, float after) {
                float top = topProperty.getValue();
                float bottom = bottomProperty.getValue();
                if (bottom >= texture.getHeight() - top) {
                    bottom = texture.getHeight() - top;
                }
                bottomProperty.setValue(bottom);
                editPanel.set(EditPanel.BOTTOM, bottom);
                updatePreview();
            }

            @Override
            public void dragged(float before, float after) {
                float top = topProperty.getValue();
                float bottom = bottomProperty.getValue();
                if (bottom >= texture.getHeight() - top) {
                    bottom = before;
                }
                bottomProperty.setValue(bottom);
                editPanel.set(EditPanel.BOTTOM, bottom);
            }

            @Override
            public void dragStop() {
                updatePreview();
            }
        });
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

        SceneEditorAddon.get().seAppManager.close(this);
    }

    @Override
    public String getTitle() {
        return "Sprite Editor";
    }

    public void setListener(SpriteMetadataListener spriteMetadataListener) {
        listener = spriteMetadataListener;
    }

    public static interface SpriteMetadataListener {
        void changed(int left, int right, int top, int bottom);
    }

    public AEditorApp show(SpriteMetadata metadata) {

        // get ninepatch
        FileHandle file = metadata.link.handle;
        texture = new Texture(file);
        // clamp metadata values in case they are invalid
        metadata.borderData[0] = MathUtils.clamp(metadata.borderData[0], 0, texture.getWidth());
        metadata.borderData[1] = MathUtils.clamp(metadata.borderData[1], 0, texture.getWidth());
        metadata.borderData[2] = MathUtils.clamp(metadata.borderData[2], 0, texture.getHeight());
        metadata.borderData[3] = MathUtils.clamp(metadata.borderData[3], 0, texture.getHeight());
        
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
        ninePatchPreview.show(patchDrawable);

        // set limits
        leftProperty.setRange(0, texture.getWidth());
        rightProperty.setRange(0, texture.getWidth());
        topProperty.setRange(0, texture.getHeight());
        bottomProperty.setRange(0, texture.getHeight());

        // set values
        leftProperty.setValue(editPanel.getLeft());
        rightProperty.setValue(editPanel.getRight());
        topProperty.setValue(editPanel.getTop());
        bottomProperty.setValue(editPanel.getBottom());

        return this;
    }

    private void updatePreview() {
        float left = editPanel.getLeft();
        float right = editPanel.getRight();
        float top = editPanel.getTop();
        float bottom = editPanel.getBottom();
        NinePatch patch = new NinePatch(
                texture,
                (int) left,
                (int) right,
                (int) top,
                (int) bottom
        );
        patchDrawable.setPatch(patch);
    }

    private static class NinePatchPreview extends Table {
        public static final float WIDTH = 175.0f;
        public static final float HEIGHT = 175.0f;

        public void show (NinePatchDrawable patchDrawable) {
            clearChildren();
            Image vertical = new Image(patchDrawable) {
                {
                    float w = NinePatchPreview.WIDTH / 4f - 5;
                    float h = 3 * NinePatchPreview.HEIGHT / 4f;
                    float ratio = h / w;
                    setSize(getWidth(), getHeight() * ratio);

                    float scaleX = w / getWidth();
                    float scaleY = h / getHeight();
                    float scale = Math.min(scaleX, scaleY);
                    setScale(scale, scale);
                    setPosition(0, NinePatchPreview.WIDTH - getHeight() * getScaleY());
                }
            };
            Image square = new Image(patchDrawable) {
                {
                    float w = 3 * NinePatchPreview.WIDTH / 4f;
                    float h = 3 * NinePatchPreview.HEIGHT / 4f;
                    float ratio = w / h;
                    setSize(getWidth() * ratio, getHeight());

                    float scaleX = w / getWidth();
                    float scaleY = h / getHeight();
                    float scale = Math.min(scaleX, scaleY);
                    setScale(scale, scale);
                    setPosition(NinePatchPreview.WIDTH - getWidth() * getScaleX(), NinePatchPreview.HEIGHT - getHeight() * getScaleY());
                }
            };
            Image horizontal = new Image(patchDrawable) {
                {
                    float w = NinePatchPreview.WIDTH;
                    float h = NinePatchPreview.HEIGHT / 4f - 5;
                    float ratio = w / h;
                    setSize(getWidth() * ratio, getHeight());

                    float scaleX = w / getWidth();
                    float scaleY = h / getHeight();
                    float scale = Math.min(scaleX, scaleY);
                    setScale(scale, scale);
                }
            };

            addActor(vertical);
            addActor(square);
            addActor(horizontal);
        }
    }
}
