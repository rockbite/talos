package com.talosvfx.talos.editor.widgets.ui.common;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Null;

public class LabelWithZoom extends Label {
    private LabelStyle providedStyle;

    public LabelWithZoom (@Null CharSequence text, Skin skin) {
        super(text, skin);
        cloneStyle();
    }
    public LabelWithZoom (@Null CharSequence text, Skin skin, String styleName) {
        super(text, skin, styleName);
        cloneStyle();
    }

    public LabelWithZoom (@Null CharSequence text, Skin skin, String fontName, Color color) {
        super(text, skin, fontName, color);
        cloneStyle();
    }

    public LabelWithZoom (@Null CharSequence text, Skin skin, String fontName, String colorName) {
        super(text, skin, fontName, colorName);
        cloneStyle();
    }

    public LabelWithZoom (@Null CharSequence text, LabelStyle style) {
        super(text, style);
        cloneStyle();
    }

    private void cloneStyle () {
        LabelStyle style = getStyle();
        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = style.font;
        if (labelStyle.fontColor != null) {
            labelStyle.fontColor = new Color(labelStyle.fontColor);
        }
        labelStyle.background = style.background;
        this.providedStyle = labelStyle;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        Stage stage = getStage();
        Camera camera = stage.getCamera();
        if (camera instanceof OrthographicCamera) {
            OrthographicCamera orthographicCamera = (OrthographicCamera) camera;
            float zoom = orthographicCamera.zoom;
            if (zoom > 1) {

            }
        }
    }
}
