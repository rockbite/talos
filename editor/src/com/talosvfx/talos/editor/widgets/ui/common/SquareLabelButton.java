package com.talosvfx.talos.editor.widgets.ui.common;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.LabelWithZoom;

public class SquareLabelButton extends Button {

    private Label buttonTextLabel;

    public SquareLabelButton(Skin skin, String text) {
        build(skin, text);
    }

    private void build(Skin skin, String buttonText) {
        setDisabled(true);
        setSkin(skin);
        setStyle(skin.get("square", ButtonStyle.class));

        setSize(24, 24);
        buttonTextLabel = new LabelWithZoom(buttonText, skin, "small");

        add(buttonTextLabel).center().padBottom(2);
    }

    public void setText(String text) {
        buttonTextLabel.setText(text);
    }

}
