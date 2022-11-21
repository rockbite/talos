package com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.draw;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class DrawableQuad implements Pool.Poolable {

    public String region;
    public Vector2 position = new Vector2();
    public Vector2 size = new Vector2();

    @Override
    public void reset() {

    }
}
