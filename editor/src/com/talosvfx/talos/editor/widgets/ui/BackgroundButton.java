package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class BackgroundButton extends Table {

    public ImageButton button;

    public BackgroundButton(Skin skin, Drawable on, Drawable off) {
        setSkin(skin);
        setBackground(getSkin().getDrawable("panel_button_bg"));

        button = new ImageButton(on, on, off);

        add(button);
    }

    public BackgroundButton(Skin skin, Drawable drawable) {
        setSkin(skin);
        setBackground(getSkin().getDrawable("panel_button_bg"));

        button = new ImageButton(drawable);

        add(button);
    }
}
