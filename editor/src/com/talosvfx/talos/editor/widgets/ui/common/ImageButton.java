package com.talosvfx.talos.editor.widgets.ui.common;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

public class ImageButton extends Table {
    private final ClickListener clickListener;
    private final Image icon;
    private final Drawable bg;

    public ImageButton(Drawable bg, Drawable iconDrawable) {
        this.bg = bg;
        icon = new Image(iconDrawable);
        icon.setTouchable(Touchable.enabled);
        add(icon).pad(5);
        setTouchable(Touchable.enabled);

        clickListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
            }
        };

        addListener(clickListener);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if(clickListener.isOver()) {
            icon.getColor().a = 1f;
        } else {
            icon.getColor().a = 0.5f;
        }

        if(clickListener.isPressed()) {
            setTransform(true);
            setOrigin(Align.center);
            setScale(0.9f);
            icon.getColor().a = 0.5f;
        } else {
            setTransform(false);
            setScale(1f);
        }

        setBackground(bg);
    }
}