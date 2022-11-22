package com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.draw;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class DrawableQuad implements Pool.Poolable {

    public Vector2 position = new Vector2();
    public Vector2 size = new Vector2();
    public Texture texture;
    public float rotation;
    public Color color;
    public boolean aspect;

    @Override
    public void reset() {

    }
}
