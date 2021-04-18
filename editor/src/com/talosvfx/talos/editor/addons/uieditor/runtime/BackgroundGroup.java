package com.talosvfx.talos.editor.addons.uieditor.runtime;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class BackgroundGroup extends Group {

    protected Drawable background;

    @Override
    public void draw (Batch batch, float parentAlpha) {
        drawBackground(batch, parentAlpha);
        super.draw(batch, parentAlpha);
    }

    public void setBackground(Drawable background) {
        this.background = background;
    }

    private void drawBackground (Batch batch, float parentAlpha) {
        if (background == null) return;
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        background.draw(batch, getX(), getY(), getWidth(), getHeight());
    }
}
