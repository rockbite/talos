package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.talosvfx.talos.TalosMain;

public class TransformGizmo extends Gizmo {

    @Override
    public void draw (Batch batch, float parentAlpha) {
        TextureRegion region = TalosMain.Instance().getSkin().getRegion("ic-target");
        float size = 30; // pixels

        size *= worldPerPixel;

        batch.draw(region, getX() - size / 2f, getY() - size / 2f, size, size);
    }
}
