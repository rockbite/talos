package com.rockbite.tools.talos.editor.data;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.ObjectSet;
import com.rockbite.tools.talos.editor.widgets.ui.EditableLabel;
import com.rockbite.tools.talos.editor.wrappers.ModuleWrapper;

public class ModuleWrapperGroup extends Group {

    private String text = "";

    private ObjectSet<ModuleWrapper> wrappers = new ObjectSet();

    private Vector2 pos = new Vector2();

    private Vector2 size = new Vector2();

    private final int PADDING = 20;
    private final int TOP_BAR = 34;

    private Skin skin;

    private Vector2 posMin = new Vector2();
    private Vector2 posMax = new Vector2();

    Image frameImage;
    EditableLabel title;
    ImageButton settings;

    public ModuleWrapperGroup(Skin skin) {
        this.skin = skin;

        frameImage = new Image(skin.getDrawable("group_frame"));
        frameImage.setColor(44/255f, 140/255f, 209/255f, 1f);
        addActor(frameImage);

        title = new EditableLabel("VELOCITY & TRANSFORM", skin);
        addActor(title);

        settings = new ImageButton(skin, "settings");
        settings.setSize(25, 25);
        addActor(settings);
    }

    public void setWrappers(ObjectSet<ModuleWrapper> wrappers) {
        this.wrappers.addAll(wrappers);
    }

    private void recalculateTransform() {
        posMin.set(wrappers.first().getX(), wrappers.first().getY());
        posMax.set(wrappers.first().getX() + wrappers.first().getWidth(), wrappers.first().getY() + wrappers.first().getHeight());
        for (ModuleWrapper wrapper : wrappers) {
            if (wrapper.getX() < posMin.x)
                posMin.x = wrapper.getX();
            if (wrapper.getY() < posMin.y)
                posMin.y = wrapper.getY();
            if (wrapper.getX() + wrapper.getWidth() > posMax.x)
                posMax.x = wrapper.getX() + wrapper.getWidth();
            if (wrapper.getY() + wrapper.getHeight() > posMax.y)
                posMax.y = wrapper.getY() + wrapper.getHeight();

        }

        pos.set(posMin).sub(PADDING, PADDING);
        size.set(posMax).sub(posMin).add(PADDING * 2, PADDING * 2);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        recalculateTransform(); //TODO: dirty logic

        setPosition(pos.x, pos.y);
        setSize(size.x, size.y);
        frameImage.setPosition(0, 0);
        frameImage.setSize(getWidth(), getHeight() + TOP_BAR);

        title.setPosition(7, getHeight() - title.getPrefHeight() + TOP_BAR - 5);
        settings.setPosition(getWidth() - settings.getWidth() - 3, getHeight() - settings.getHeight() + TOP_BAR - 3);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }
}
