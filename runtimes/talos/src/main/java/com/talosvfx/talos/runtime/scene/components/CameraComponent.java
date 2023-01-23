package com.talosvfx.talos.runtime.scene.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.runtime.scene.ValueProperty;

public class CameraComponent extends AComponent {

    public Color backgroundColor = new Color(Color.valueOf("1e3357ff"));

    @ValueProperty(min=0.01f, max=10f, step=0.01f)
    public float zoom = 1f;

    @ValueProperty(prefix = {"W", "H"})
    public Vector2 size = new Vector2(6, 4);

    @Override
    public void reset() {
        super.reset();
        zoom = 1;
        size.set(6,6);
        backgroundColor.set(Color.valueOf("1e3357ff"));
    }
}
