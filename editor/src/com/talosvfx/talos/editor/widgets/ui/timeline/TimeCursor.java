package com.talosvfx.talos.editor.widgets.ui.timeline;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class TimeCursor extends Group {

    private Image head;
    private Image line;

    public TimeCursor(Skin skin) {
        setTransform(false);

        head = new Image(skin.getDrawable("timeline-time-selector"));

        line = new Image(skin.newDrawable("timeline-white-bg"));

        head.setWidth(9);
        head.setHeight(23);
        line.setWidth(1);

        line.setColor(ColorLibrary.BLUE);

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
}
