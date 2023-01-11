package com.talosvfx.talos.editor.addons.scene.apps.routines.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class DeleteButton extends Table {
    private final ClickListener clickListener;
    private final Image icon;
    private final Drawable bg;

    public DeleteButton() {
        this.bg = SharedResources.skin.newDrawable(ColorLibrary.SHAPE_SQUARE, ColorLibrary.BackgroundColor.LIGHT_GRAY.getColor());
        icon = new Image(SharedResources.skin.newDrawable("icon-close", Color.WHITE));
        icon.setTouchable(Touchable.enabled);
        icon.setScale(0.9f);
        icon.setOrigin(Align.center);
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
            icon.getColor().a = 0.8f;
        } else {
            icon.getColor().a = 0.5f;
        }


        if(clickListener.isPressed()) {
            icon.getColor().a = 1f;
        }

        setBackground(bg);
    }
}