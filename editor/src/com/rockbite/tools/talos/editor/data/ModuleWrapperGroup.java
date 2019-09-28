package com.rockbite.tools.talos.editor.data;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.ObjectSet;
import com.rockbite.tools.talos.editor.wrappers.ModuleWrapper;

public class ModuleWrapperGroup extends Actor {

    private String text = "";

    private ObjectSet<ModuleWrapper> wrappers = new ObjectSet();

    private Vector2 pos = new Vector2();

    private Vector2 size = new Vector2();

    private final int PADDING = 20;

    private Skin skin;

    private Vector2 posMin = new Vector2();
    private Vector2 posMax = new Vector2();

    public ModuleWrapperGroup(Skin skin) {
        this.skin = skin;
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
    public void draw(Batch batch, float parentAlpha) {
        recalculateTransform();
        Drawable bg = skin.getDrawable("orange_row");
        if(wrappers.size > 0) {
            bg.draw(batch, getX() + pos.x, getY() + pos.y, size.x, size.y);
        }
    }
}
