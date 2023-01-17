package com.talosvfx.talos.editor.widgets.ui.common;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Null;
import com.talosvfx.talos.editor.utils.UIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LabelWithZoom extends Label {

    private static final Logger logger = LoggerFactory.getLogger(LabelWithZoom.class);

    private LabelStyle providedStyle;

    private UIUtils.RegisteredFont registeredFont;

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
        UIUtils.RegisteredFont fontByName = UIUtils.RegisteredFont.getFontByName(labelStyle.font.toString());
        if (fontByName == null) {
            logger.error("No font found with name " + labelStyle.font.toString());
        }
        this.registeredFont = fontByName;
        this.setStyle(providedStyle);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
//        getStyle().font.getData().
//        Stage stage = getStage();
//        if (stage != null) {
//            Camera camera = stage.getCamera();
//            if (camera instanceof OrthographicCamera) {
//                OrthographicCamera orthographicCamera = (OrthographicCamera) camera;
//                float preferredFontSize = worldToPixel(orthographicCamera, registeredFont.defaultSize);
//                int fontSize = UIUtils.getClosestFontSize(preferredFontSize);
//                BitmapFont bitmapFont = UIUtils.orderedFontMap.get(fontSize);
//                getStyle().font = bitmapFont;
//                setStyle(getStyle());
//                setFontScale(registeredFont.defaultSize / preferredFontSize);
//            }
//        }
    }

    private Vector3 tmp = new Vector3();

    private float worldToPixel(Camera camera, int pixelSize) {
        tmp.set(0, 0, 0);
        camera.project(tmp);
        float baseline = tmp.x;

        tmp.set(pixelSize, 0, 0);
        camera.project(tmp);
        float pos = tmp.x;

        return Math.abs(pos - baseline);
    }
}
