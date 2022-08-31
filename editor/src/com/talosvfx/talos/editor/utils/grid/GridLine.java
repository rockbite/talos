package com.talosvfx.talos.editor.utils.grid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class GridLine {
    public Vector2 startCoordinate;
    public Vector2 endCoordinate;
    public Color color;
    public float thickness;

    public GridLine (Vector2 startCoordinate, Vector2 endCoordinate, Color color, float thickness) {
        this.startCoordinate = startCoordinate;
        this.endCoordinate = endCoordinate;
        this.color = color;
        this.thickness = thickness;
    }
}
