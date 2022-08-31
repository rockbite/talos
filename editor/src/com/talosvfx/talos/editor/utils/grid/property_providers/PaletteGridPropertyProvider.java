package com.talosvfx.talos.editor.utils.grid.property_providers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.maps.TalosLayer;
import com.talosvfx.talos.editor.utils.grid.GridLine;
import com.talosvfx.talos.editor.utils.grid.GridPropertyProvider;

public class PaletteGridPropertyProvider implements GridPropertyProvider {

    private Array<GridLine> gridLines = new Array<>();

    private Color backgroundColor = new Color();

    float thickness = 1;

    private boolean shouldHighlightCursorHover = false;
    private boolean shouldHighlightCursorSelect = false;

    private int subdivisions = 0;

    private float startX;
    private float startY;
    private float endX;
    private float endY;

    @Override
    public void update (OrthographicCamera camera, float alpha) {
        gridLines.clear();
        float gridSizeX = getUnitX();
        float gridSizeY = getUnitY();

        float totalWidth = camera.viewportWidth;
        float totalHeight = camera.viewportHeight;

        totalWidth *= camera.zoom;
        totalHeight *= camera.zoom;

        float leftSide = camera.position.x - totalWidth / 2;
        float bottomSide = camera.position.y - totalHeight / 2;

        startX = ((int)(leftSide / gridSizeX)) * gridSizeX;
        startY = ((int)(bottomSide / gridSizeY)) * gridSizeY;

        Color color = new Color(1, 1, 1, 0.2f);
        Color subDivisionColour = new Color(1, 1, 1, 0.1f);

        Gdx.gl.glEnable(GL20.GL_BLEND);


        for (float x = startX; x < startX + totalWidth; x += gridSizeX) {
            if (x == 0) {
                color = Color.CHARTREUSE;
            } else {
                color = new Color(1, 1, 1, 0.2f) ;
            }
            gridLines.add(new GridLine(new Vector2(x, bottomSide), new Vector2(x, bottomSide + totalHeight), color, thickness));
            endX = x;
        }

        for (float y = startY; y < startY + totalHeight; y += gridSizeY) {
            if (y == 0) {
                color = Color.CHARTREUSE;
            } else {
                color = new Color(1, 1, 1, 0.2f) ;
            }
            gridLines.add(new GridLine(new Vector2(leftSide, y), new Vector2(leftSide + totalWidth, y), color, thickness));
            endY = y;
        }

        if (subdivisions > 0) {
            float spacePerSubdivisionX = gridSizeX / (subdivisions + 1);
            float spacePerSubdivisionY = gridSizeY / (subdivisions + 1);

            for (float x = startX; x < startX + totalWidth; x += gridSizeX) {
                for (int i = 0; i < subdivisions; i++) {
                    gridLines.add(new GridLine(new Vector2(x + spacePerSubdivisionX, bottomSide),
                            new Vector2(x + spacePerSubdivisionX, bottomSide + totalHeight), subDivisionColour, thickness));
                }
            }
            for (float y = startY; y < startY + totalHeight; y += gridSizeY) {
                for (int i = 0; i < subdivisions; i++) {
                    gridLines.add(new GridLine(new Vector2(leftSide, y + spacePerSubdivisionY),
                            new Vector2(leftSide + totalWidth, y + spacePerSubdivisionY), subDivisionColour, thickness));
                }
            }
        }
    }

    @Override
    public Array<GridLine> getGridLines () {
        return gridLines;
    }

    @Override
    public Color getBackgroundColor () {
        return backgroundColor;
    }

    @Override
    public float getUnitX () {
        if (SceneEditorWorkspace.getInstance().mapEditorState.isEditing()) {
            TalosLayer selectedLayer = SceneEditorWorkspace.getInstance().mapEditorState.getLayerSelected();

            if (selectedLayer == null) {
                return 1;
            } else {
                return selectedLayer.getTileSizeX();
            }
        }

        return 1;
    }

    @Override
    public float getUnitY () {
        if (SceneEditorWorkspace.getInstance().mapEditorState.isEditing()) {
            TalosLayer selectedLayer = SceneEditorWorkspace.getInstance().mapEditorState.getLayerSelected();

            if (selectedLayer == null) {
                return 1;
            } else {
                return selectedLayer.getTileSizeY();
            }
        }

        return 1;
    }

    @Override
    public float getGridStartX () {
        return startX;
    }

    @Override
    public float getGridEndX () {
        return endX;
    }

    @Override
    public float getGridStartY () {
        return startY;
    }

    @Override
    public float getGridEndY () {
        return endY;
    }

    @Override
    public void setLineThickness (float thickness) {
        this.thickness = thickness;
    }

    @Override
    public boolean shouldHighlightCursorHover () {
        return shouldHighlightCursorHover;
    }

    @Override
    public boolean shouldHighlightCursorSelect () {
        return shouldHighlightCursorSelect;
    }

    @Override
    public void setHighlightCursorHover (boolean shouldHighlight) {
        this.shouldHighlightCursorHover = shouldHighlight;
    }

    @Override
    public void setHighlightCursorSelect (boolean shouldHighlight) {
        this.shouldHighlightCursorSelect = shouldHighlight;
    }

    @Override
    public boolean rulerOnBottom () {
        return true;
    }
}
