package com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Null;
import com.talosvfx.talos.editor.utils.UIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LabelWithZoom extends Label {

    private static final Logger logger = LoggerFactory.getLogger(LabelWithZoom.class);
    public boolean debugScale;

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
        LabelStyle labelStyle = new LabelStyle(style);
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
        Stage stage = getStage();
        if (stage != null) {
            float preferredFontSize = stageToScreen(registeredFont.defaultSize);
            if (preferredFontSize == 0 || Float.isNaN(preferredFontSize)) {
                //Not ready yet
                return;
            }

            int fontToGenerate = MathUtils.ceil(preferredFontSize);
            fontToGenerate = MathUtils.clamp(fontToGenerate, 4, 90);

            BitmapFont font = getStyle().font;
            BitmapFont newFont = UIUtils.getFontForSize(fontToGenerate);
            if (!(font == newFont)) {
                getStyle().font = newFont;
                setStyle(getStyle());
                invalidateHierarchy();
            }

            float fontScale = preferredFontSize / fontToGenerate;
            float finalScale = screenToStage(fontScale);
            float fontScaleX = getFontScaleX();
            if (finalScale != fontScaleX) {
                setFontScale(finalScale);
                invalidateHierarchy();
            }

        }
    }

    private Vector2 tmp = new Vector2();

    private float stageToScreen(float stageSize) {
        tmp.set(0, 0);
        getStage().stageToScreenCoordinates(tmp);
        float baseline = tmp.x;

        tmp.set(stageSize, 0);
        getStage().stageToScreenCoordinates(tmp);
        float pos = tmp.x;

        return Math.abs(pos - baseline);
    }

    private float screenToStage (float screenSize) {
        tmp.set(0, 0);
        getStage().screenToStageCoordinates(tmp);
        float baseline = tmp.x;

        tmp.set(screenSize, 0);
        getStage().screenToStageCoordinates(tmp);
        float pos = tmp.x;

        return Math.abs(pos - baseline);
    }
}
