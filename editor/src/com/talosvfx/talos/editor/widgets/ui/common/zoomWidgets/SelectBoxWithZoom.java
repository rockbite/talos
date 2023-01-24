package com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.talosvfx.talos.editor.utils.UIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectBoxWithZoom<T> extends SelectBox<T> {

    private static final Logger logger = LoggerFactory.getLogger(TextFieldWithZoom.class);

    SelectBox.SelectBoxStyle providedStyle;
    UIUtils.RegisteredFont registeredFont;
    public SelectBoxWithZoom(Skin skin) {
        super(skin);
        cloneStyle();
    }

    public SelectBoxWithZoom(Skin skin, String styleName) {
        super(skin, styleName);
        cloneStyle();
    }

    public SelectBoxWithZoom(SelectBoxStyle style) {
        super(style);
        cloneStyle();
    }

    private void cloneStyle () {
        SelectBox.SelectBoxStyle style = getStyle();
        SelectBox.SelectBoxStyle textFieldStyle = new SelectBox.SelectBoxStyle(style);
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
            newFont.getData().setScale(finalScale);
            invalidateHierarchy();
        }
    }

}
