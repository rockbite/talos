package com.talosvfx.talos.editor.widgets.ui.common;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

public class SquareButton extends Button {

    private Image icon;
    private Cell iconCell;

    public SquareButton(Skin skin, Drawable drawable, String tooltip) {
        build(skin, drawable, false);
        //addListener(new TextTooltip(tooltip, skin)); //bring this back when it works well
    }

    public SquareButton(Skin skin, Label label, String tooltip) {
        setSkin(skin);
        ButtonStyle square = skin.get("square", ButtonStyle.class);
        setStyle(square);

        label.setAlignment(Align.center);

        iconCell = add(label).center().pad(5).padLeft(10).padRight(10);

        addListener(new TextTooltip(tooltip, skin));
    }

    public SquareButton(Skin skin, Drawable drawable, boolean toggle, String tooltip) {
        build(skin, drawable, toggle);
        addListener(new TextTooltip(tooltip, skin));
    }

    private void build(Skin skin, Drawable drawable, boolean toggle) {
        setSkin(skin);
        ButtonStyle square = skin.get("square", ButtonStyle.class);
        setStyle(square);
        if(!toggle) {
           setDisabled(true);
        }
        setSize(24, 24);

        icon = new Image(drawable);
        icon.setOrigin(Align.center);

        iconCell = add(icon).center().padBottom(2);
    }

    public void flipHorizontal() {
        icon.setRotation(icon.getRotation() + 180);
    }

    public void flipVertical() {
        icon.setRotation(icon.getRotation() + 90);
    }

    public Cell getIconCell() {
        return iconCell;
    }
}
