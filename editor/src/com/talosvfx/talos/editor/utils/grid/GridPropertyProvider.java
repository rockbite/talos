package com.talosvfx.talos.editor.utils.grid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Array;

public interface GridPropertyProvider {

    void update (OrthographicCamera camera, float alpha);

    Array<GridLine> getGridLines ();

    Color getBackgroundColor ();

    float getUnitX ();

    float getUnitY ();

    float getGridStartX ();

    float getGridEndX ();

    float getGridStartY ();

    float getGridEndY ();

    void setLineThickness (float thickness);

    boolean shouldHighlightCursorHover ();

    boolean shouldHighlightCursorSelect ();

    void setHighlightCursorHover (boolean shouldHighlight);
    void setHighlightCursorSelect (boolean shouldHighlight);

    boolean rulerOnBottom ();
}


