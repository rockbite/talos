package com.talosvfx.talos.editor.widgets.ui.common;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.editor.widgets.ClippedNinePatchDrawable;

public class RoundedFlatButton extends Button {

    private Image icon;
    private Cell iconCell;


    private void makeStyle(Skin skin) {
        Button.ButtonStyle style = new Button.ButtonStyle();
        style.up = ColorLibrary.createClippedPatch(skin, ColorLibrary.SHAPE_SQUIRCLE, ColorLibrary.BackgroundColor.LIGHT_GRAY);
        style.down = ColorLibrary.createClippedPatch(skin, ColorLibrary.SHAPE_SQUIRCLE, ColorLibrary.BackgroundColor.LIGHT_BLUE);
        style.over = ColorLibrary.createClippedPatch(skin, ColorLibrary.SHAPE_SQUIRCLE, ColorLibrary.BackgroundColor.BRIGHT_GRAY);
        style.disabled = ColorLibrary.createClippedPatch(skin, ColorLibrary.SHAPE_SQUIRCLE, ColorLibrary.BackgroundColor.BRIGHT_GRAY);
        setStyle(style);
    }

    public RoundedFlatButton(Skin skin, Drawable drawable) {
        build(skin, drawable, false);
    }

    public RoundedFlatButton(Skin skin, Label label) {
        setSkin(skin);
        makeStyle(skin);

        label.setAlignment(Align.center);

        iconCell = add(label).center().pad(5).padLeft(10).padRight(10);

    }

    public RoundedFlatButton(Skin skin, Drawable drawable, boolean toggle) {
        build(skin, drawable, toggle);
    }

    private void build(Skin skin, Drawable drawable, boolean toggle) {
        setSkin(skin);
        makeStyle(skin);

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
