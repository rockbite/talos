package com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Null;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
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
    public void act(float delta) {
        Stage stage = getStage();
        if (stage != null) {
            BitmapFont defaultSizedFont = UIUtils.getFontForSize(registeredFont.defaultSize);
            GlyphLayout glyphForFont = UIUtils.getGlyphForFont(defaultSizedFont);
            float height = glyphForFont.height;

            float preferredHeight = UIUtils.stageToScreen(getStage(), height);
            if (preferredHeight == 0 || Float.isNaN(preferredHeight)) {
                //Not ready yet
                return;
            }
            BitmapFont testFont = UIUtils.getFontForSize(4);
            for (int i = 5; i <= 90; i++) {
                testFont = UIUtils.getFontForSize(i);
                GlyphLayout glyphForTest = UIUtils.getGlyphForFont(testFont);
                if (glyphForTest.height >= preferredHeight) {
                    break;
                }
            }

            BitmapFont font = getStyle().font;
            BitmapFont newFont = testFont;
            if (!(font == newFont)) {
                getStyle().font = newFont;
                setStyle(getStyle());
                invalidateHierarchy();
            }

            float fontScale = preferredHeight / UIUtils.getGlyphForFont(testFont).height;
            float finalScale = UIUtils.screenToStage(getStage(), fontScale);
            float fontScaleX = getFontScaleX();
            if (finalScale != fontScaleX) {
                setFontScale(finalScale);
                invalidateHierarchy();
            }
        }
        super.act(delta);
    }
}
