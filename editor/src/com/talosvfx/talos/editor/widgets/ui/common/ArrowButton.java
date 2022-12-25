package com.talosvfx.talos.editor.widgets.ui.common;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.talosvfx.talos.editor.project2.SharedResources;
import lombok.Getter;

public class ArrowButton extends Table {
    private final ClickListener clickListener;
    @Getter
    private final Image arrowIcon;

    @Getter
    private boolean isCollapsed = true;
    public boolean hasBackground;

    public ArrowButton () {
        this(true);
    }
    public ArrowButton(boolean hasBackground) {
        this.hasBackground = hasBackground;
        arrowIcon = new Image();
        arrowIcon.setDrawable(SharedResources.skin.getDrawable("mini-arrow-right"));
        arrowIcon.setTouchable(Touchable.enabled);

        add(arrowIcon).pad(5);

        setTouchable(Touchable.enabled);

        clickListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
            }
        };

        addListener(clickListener);
    }

    public void setCollapsed(boolean isCollapsed) {
        this.isCollapsed = isCollapsed;

        if(isCollapsed) {
            arrowIcon.setDrawable(SharedResources.skin.getDrawable("mini-arrow-right"));
        } else {
            arrowIcon.setDrawable(SharedResources.skin.getDrawable("mini-arrow-down"));
        }
    }

    public void toggle() {
        setCollapsed(!isCollapsed);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (hasBackground) {
            // change background if mouse is over
            ColorLibrary.BackgroundColor color = ColorLibrary.BackgroundColor.BRIGHT_GRAY;
            if(!clickListener.isOver()) {
                color = ColorLibrary.BackgroundColor.LIGHT_GRAY;
            }
            setBackground(ColorLibrary.obtainBackground(SharedResources.skin, ColorLibrary.SHAPE_SQUARE, color));
        }
    }
}
