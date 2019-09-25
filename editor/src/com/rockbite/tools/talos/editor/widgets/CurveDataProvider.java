package com.rockbite.tools.talos.editor.widgets;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public interface CurveDataProvider {

    Array<Vector2> getPoints();
    void removePoint(int index);
    int createPoint(float x, float y);
}
