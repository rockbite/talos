package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.math.Vector2;

public interface IDragPointProvider {

    Vector2[] fetchDragPoints();

    void dragPointChanged(Vector2 point);
}
