package com.rockbite.tools.talos.editor.widgets.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class DragPoint {
    public Vector2 position = new Vector2();
    public Vector2 origin = new Vector2();
    public Color color = new Color();
    public boolean drawOrigin = false;

    public DragPoint() {

    }

    public DragPoint(float x, float y) {
        position.set(x, y);
        drawOrigin = false;
        color.set(Color.ORANGE);
    }

    public void set(float x, float y) {
        position.set(x, y);
    }
}
