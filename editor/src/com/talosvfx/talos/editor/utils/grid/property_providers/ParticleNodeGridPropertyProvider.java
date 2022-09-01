package com.talosvfx.talos.editor.utils.grid.property_providers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.utils.grid.GridLine;
import com.talosvfx.talos.editor.utils.grid.GridPropertyProvider;

public class ParticleNodeGridPropertyProvider implements GridPropertyProvider {

    private Vector2 tmp = new Vector2();

    private Array<GridLine> gridLines = new Array<>();

    private Color backgroundColor = new Color();

    private float tileSize = 15f;

    private float thickness = 3f;

    @Override
    public void update (OrthographicCamera camera, float alpha) {
        gridLines.clear();

        final float worldWidth = camera.viewportWidth * camera.zoom;
        final float worldHeight = camera.viewportHeight * camera.zoom;

        tmp.set(camera.position.x, camera.position.y);

        int lineCount = (int)(worldWidth / tileSize);
        int blackLineCount = (int)(worldWidth / (tileSize * 10));
        float width = worldWidth;

        Color mainColor = new Color(0.17f, 0.17f, 0.17f, 1f);
        for (int i = -lineCount / 2 - 1; i < lineCount / 2 + 1; i++) {
            float spacing = width / lineCount;
            thickness = 2f * camera.zoom;
            float posX = tmp.x - i * spacing - tmp.x % spacing;
            float posY = tmp.y + i * spacing - tmp.y % spacing;
            gridLines.add(new GridLine(new Vector2(posX, tmp.y - worldHeight/2f),
                    new Vector2(posX, tmp.y + worldHeight/2f), mainColor, thickness));
            gridLines.add(new GridLine(new Vector2(tmp.x - worldWidth/2f, posY),
                    new Vector2(tmp.x + worldWidth/2f, posY), mainColor, thickness));
        }

        Color blackLinesColor = new Color(0.12f, 0.12f, 0.12f, 1f);
        for (int i = -blackLineCount / 2 - 1; i < blackLineCount / 2 + 1; i++) {
            float spacing = width / blackLineCount;
            thickness = 3f * camera.zoom;
            float posX = tmp.x - i * spacing - tmp.x % spacing;
            float posY = tmp.y + i * spacing - tmp.y % spacing;
            gridLines.add(new GridLine(new Vector2(posX, tmp.y - worldHeight/2f),
                    new Vector2(posX, tmp.y + worldHeight/2f), blackLinesColor, thickness));
            gridLines.add(new GridLine(new Vector2(tmp.x - worldWidth/2f, posY),
                    new Vector2(tmp.x + worldWidth/2f, posY), blackLinesColor, thickness));
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
        return tileSize;
    }

    @Override
    public float getUnitY () {
        return tileSize;
    }

    @Override
    public float getGridStartX () {
        return 0;
    }

    @Override
    public float getGridEndX () {
        return 0;
    }

    @Override
    public float getGridStartY () {
        return 0;
    }

    @Override
    public float getGridEndY () {
        return 0;
    }

    @Override
    public void setLineThickness (float thickness) {
        this.thickness = thickness;
    }

    @Override
    public boolean shouldHighlightCursorHover () {
        return false;
    }

    @Override
    public boolean shouldHighlightCursorSelect () {
        return false;
    }

    @Override
    public void setHighlightCursorHover (boolean shouldHighlight) {

    }

    @Override
    public void setHighlightCursorSelect (boolean shouldHighlight) {

    }
    @Override
    public boolean rulerOnBottom () {
        return false;
    }
}
