package com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.talosvfx.talos.editor.utils.UIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextFieldWithZoom extends TextField {

    TextFieldStyle providedStyle;
    UIUtils.RegisteredFont registeredFont;

    private static final Logger logger = LoggerFactory.getLogger(TextFieldWithZoom.class);

    public TextFieldWithZoom(String text, Skin skin) {
        super(text, skin);
        cloneStyle();
    }

    public TextFieldWithZoom(String text, Skin skin, String styleName) {
        super(text, skin, styleName);
        cloneStyle();
    }

    public TextFieldWithZoom(String text, TextFieldStyle style) {
        super(text, style);
        cloneStyle();
    }

    Vector2 tmp = new Vector2();

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

    private void cloneStyle () {
        TextField.TextFieldStyle style = getStyle();
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle(style);
        this.providedStyle = textFieldStyle;
        UIUtils.RegisteredFont fontByName = UIUtils.RegisteredFont.getFontByName(textFieldStyle.font.toString());
        if (fontByName == null) {
            logger.error("No font found with name " + textFieldStyle.font.toString());
        }
        this.registeredFont = fontByName;
        this.setStyle(textFieldStyle);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        Stage stage = getStage();

        if (stage != null) {
            BitmapFont defaultSizedFont = UIUtils.getFontForSize(registeredFont.defaultSize);
            GlyphLayout glyphForFont = UIUtils.getGlyphForFont(defaultSizedFont);
            float height = glyphForFont.height;

            float preferredHeight = stageToScreen(height);
            if (preferredHeight == 0 || Float.isNaN(preferredHeight)) {
                //Not ready yet
                return;
            }
            BitmapFont testFont = UIUtils.getFontForSize(4);
            for (int i = 5; i <= 90; i++) {
                testFont = UIUtils.getFontForSize(i);
                GlyphLayout glyphForTest = UIUtils.getGlyphForFont(testFont);
                if (glyphForTest.height > preferredHeight) {
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
            float finalScale = screenToStage(fontScale);
            newFont.getData().setScale(finalScale);
            invalidateHierarchy();
        }
    }
}
