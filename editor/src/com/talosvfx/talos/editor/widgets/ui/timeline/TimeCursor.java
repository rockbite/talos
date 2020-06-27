package com.talosvfx.talos.editor.widgets.ui.timeline;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class TimeCursor extends Table {

    private Image head;
    private Image line;

    Vector2 tmp = new Vector2();

    public TimeCursor(Skin skin) {
        setTransform(false);

        head = new Image(skin.getDrawable("timeline-time-selector"));

        line = new Image(skin.newDrawable("timeline-white-bg"));

        head.setWidth(9);
        head.setHeight(23);
        line.setWidth(1);

        line.setColor(ColorLibrary.BLUE);

        addListener(new ClickListener() {

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
            }
        });

        addActor(head);
        addActor(line);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if(getParent() == null) return;

        head.setPosition(getX(), getParent().getHeight() - head.getHeight());

        line.setPosition(getX() + head.getWidth()/2f, 18);
        line.setHeight(getParent().getHeight() - head.getHeight()-18);
    }

    @Override
    public Actor hit (float x, float y, boolean touchable) {
        if (touchable && getTouchable() == Touchable.disabled) return null;
        if (!isVisible()) return null;

        tmp.set(getX(), getParent().getHeight() - head.getHeight());
        if (tmp.dst(x, y) < 20) {
            return this;
        }

        return null;
    }
}
