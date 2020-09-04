package com.talosvfx.talos.editor.widgets.ui.timeline;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class TimeCursor extends Table {

    private final Image head;
    private final Label label;
    private Table cursorTable;
    private Image line;

    Vector2 tmp = new Vector2();

    public TimeCursor(Skin skin) {
        setTransform(false);
        setSkin(skin);

        cursorTable = new Table();

        head = new Image(skin.getDrawable("timeline-time-selector"));

        cursorTable.setBackground(skin.getDrawable("timeline-time-indicator-bg"));

        label = new Label("00 : 00", getSkin());
        label.setFontScale(0.65f);
        cursorTable.add(label).padLeft(6).padRight(6).padTop(0).padBottom(1.5f).left().expandX();
        cursorTable.pack();

        head.setSize(9, 23);
        line = new Image(skin.newDrawable("timeline-white-bg"));
        line.setWidth(1);
        line.setColor(ColorLibrary.BLUE);

        setTouchable(Touchable.disabled);

        addActor(head);
        addActor(line);
        addActor(cursorTable);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        updatePos();
    }

    private void updatePos() {
        if(getParent() == null) return;
        head.setPosition(getX(), getParent().getHeight() - head.getHeight());

        line.setPosition(getX() + head.getWidth()/2f, 18);
        line.setHeight(getParent().getHeight() - head.getHeight()-18);

        cursorTable.setPosition(getX() + head.getWidth() + 1, getParent().getHeight() - head.getHeight() - 1);
    }

    @Override
    public void setPosition (float x, float y) {
        super.setPosition(x, y);
        updatePos();
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

    public void setLabelValue(String text) {
        label.setText(text);
    }
}
