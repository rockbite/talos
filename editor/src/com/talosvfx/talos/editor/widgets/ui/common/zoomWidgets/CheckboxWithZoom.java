package com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class CheckboxWithZoom extends CheckBox {
    public CheckboxWithZoom(String text, Skin skin) {
        super(text, skin);
    }

    public CheckboxWithZoom(String text, Skin skin, String styleName) {
        super(text, skin, styleName);
    }

    @Override
    protected Label newLabel(String text, Label.LabelStyle style) {
        return new LabelWithZoom(text, style);
    }
}
