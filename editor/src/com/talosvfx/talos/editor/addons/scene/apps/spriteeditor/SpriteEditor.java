package com.talosvfx.talos.editor.addons.scene.apps.spriteeditor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;
import com.talosvfx.talos.editor.project2.SharedResources;

public class SpriteEditor extends WidgetGroup {

    private SpriteMetadataListener listener;

    public EditPanel editPanel;
    private Table rightSide;

    private NinePatchPreview ninePatchPreview;
    private NumberPanel leftProperty;
    private NumberPanel rightProperty;
    private NumberPanel topProperty;
    private NumberPanel bottomProperty;

    private Texture texture;
    private NinePatchDrawable patchDrawable;

    private final float pad = 40.0f;
    private final float space = 20.0f;

    private float width = 0;
    private float height = 0;

    public SpriteEditor() {
        super();
        init();
    }

    public void init() {
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
        rightSide = new Table();
        ninePatchPreview = new NinePatchPreview();
        Table numberControls = new Table();

        Label leftLabel = new Label("Left: ", SharedResources.skin);
        leftProperty = new NumberPanel();
        Label rightLabel = new Label("Right: ", SharedResources.skin);
        rightProperty = new NumberPanel();
        Label topLabel = new Label("Top: ", SharedResources.skin);
        topProperty = new NumberPanel();
        Label bottomLabel = new Label("Bottom: ", SharedResources.skin);
        bottomProperty = new NumberPanel();

        TextButton saveSpriteMetaData = new TextButton("Save", SharedResources.skin);
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

        addActor(editPanel);
        addActor(rightSide);

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
            editPanel.resetOffsets();
        }
    }

    public void setListener(SpriteMetadataListener spriteMetadataListener) {
        listener = spriteMetadataListener;
    }

    public interface SpriteMetadataListener {
        void changed(int left, int right, int top, int bottom);
    }

    public void show(GameAsset<Texture> gameAsset) {
        // get ninepatch
        texture = gameAsset.getResource();
        SpriteMetadata metadata = (SpriteMetadata) gameAsset.getRootRawAsset().metaData;

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
                    float width = patchDrawable.getMinWidth();
                    float height =  patchDrawable.getMinHeight();
                    float upscaled_width, upscaled_height;
                    if (width < height) {
                        upscaled_width = width;
                        upscaled_height = height * ( h / w);
                    } else {
                        upscaled_width = width;
                        upscaled_height = height * ((width * h) / (height * w));
                    }
                    setSize(upscaled_width, upscaled_height);
                    setScale(w / getWidth(), h / getHeight());
                    setPosition(0, NinePatchPreview.WIDTH - getHeight() * getScaleY());
                }
            };
            Image square = new Image(patchDrawable) {
                {
                    float w = 3 * NinePatchPreview.WIDTH / 4f;
                    float h = 3 * NinePatchPreview.HEIGHT / 4f;
                    float longestSide = Math.max(patchDrawable.getMinWidth(), patchDrawable.getMinHeight());
                    setSize(longestSide, longestSide);

                    float scale = Math.min(w / longestSide, h / longestSide);
                    setScale(scale, scale);
                    setPosition(NinePatchPreview.WIDTH - getWidth() * getScaleX(), NinePatchPreview.HEIGHT - getHeight() * getScaleY());
                }
            };
            Image horizontal = new Image(patchDrawable) {
                {
                    float w = NinePatchPreview.WIDTH;
                    float h = NinePatchPreview.HEIGHT / 4f - 5;
                    float width = patchDrawable.getMinWidth();
                    float height =  patchDrawable.getMinHeight();
                    float upscaled_width, upscaled_height;
                    if (width > height) {
                        upscaled_width = width * ((w/height) / (h/width));
                        upscaled_height = height;
                    } else {
                        upscaled_width = width * (w / h);
                        upscaled_height = height;
                    }
                    setSize(upscaled_width, upscaled_height);
                    setScale(w / getWidth(), h / getHeight());
                }
            };

            addActor(vertical);
            addActor(square);
            addActor(horizontal);
        }
    }
    public static int gcd(int a, int b) { return b==0 ? a : gcd(b, a%b); }
    @Override
    public void layout() {
        super.layout();
        float remainingWidth = getWidth() - 2 * pad - editPanel.getPrefWidth() - rightSide.getPrefWidth();
        if (remainingWidth < 0) {
            layoutWrapped();
            return;
        }

        // position the right side
        rightSide.setPosition(getWidth() - rightSide.getPrefWidth() / 2f - pad, rightSide.getPrefHeight() / 2f + pad);
        editPanel.setPosition(pad, pad);
        editPanel.setSize(getWidth() - 2.0f * pad - rightSide.getPrefWidth() - space, getHeight() - 2.0f * pad);
    }

    private void layoutWrapped () {
        rightSide.setPosition(pad + rightSide.getPrefWidth() / 2f, pad + rightSide.getPrefHeight() / 2f);
        float remainingHeight = getHeight() - 2.0f * pad - space - rightSide.getPrefHeight();
        remainingHeight = Math.max(remainingHeight, editPanel.getPrefHeight());
        editPanel.setSize(getWidth() - 2.0f * pad, remainingHeight);
        editPanel.setPosition(pad, getHeight() - pad - remainingHeight);
    }

    @Override
    public float getPrefWidth() {
        return width;
    }

    public float getPrefHeight() {
        return height;
    }
}
