package com.talosvfx.talos.editor.utils.grid.property_providers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.maps.TalosLayer;
import com.talosvfx.talos.editor.utils.grid.GridLine;
import com.talosvfx.talos.editor.utils.grid.GridPropertyProvider;

public class PaletteGridPropertyProvider implements GridPropertyProvider {

    @Override
    public void update (OrthographicCamera camera, float alpha) {

    }

    @Override
    public Array<GridLine> getGridLines () {
//        float[] floats = gridProperties.sizeProvider.get();
//
//        float gridSizeX = floats[0];
//        float gridSizeY = floats[1];
//        int subdivisions = gridProperties.subdivisions;
//
//        shapeRenderer.setProjectionMatrix(camera.combined);
//
//        float totalWidth = camera.viewportWidth;
//        float totalHeight = camera.viewportHeight;
//
//        totalWidth *= camera.zoom;
//        totalHeight *= camera.zoom;
//
//        float leftSide = camera.position.x - totalWidth / 2;
//        float bottomSide = camera.position.y - totalHeight / 2;
//
//        float startGridX = ((int)(leftSide / gridSizeX)) * gridSizeX;
//        float startGridY = ((int)(bottomSide / gridSizeY)) * gridSizeY;
//
//        Color color = new Color(1, 1, 1, 0.2f);
//        Color subDivisionColour = new Color(1, 1, 1, 0.1f);
//
//        Gdx.gl.glEnable(GL20.GL_BLEND);
//        shapeRenderer.setColor(color);
//        shapeRenderer.setAutoShapeType(true);
//        shapeRenderer.begin();
//
//
//        for (float x = startGridX; x < startGridX + totalWidth; x += gridSizeX) {
//            shapeRenderer.line(x, bottomSide, x, bottomSide + totalHeight);
//        }
//        for (float y = startGridY; y < startGridY + totalHeight; y += gridSizeY) {
//            shapeRenderer.line(leftSide, y, leftSide + totalWidth, y);
//        }
//        if (subdivisions > 0) {
//            float spacePerSubdivisionX = gridSizeX / (subdivisions + 1);
//            float spacePerSubdivisionY = gridSizeY / (subdivisions + 1);
//
//            shapeRenderer.setColor(subDivisionColour);
//            for (float x = startGridX; x < startGridX + totalWidth; x += gridSizeX) {
//                for (int i = 0; i < subdivisions; i++) {
//                    shapeRenderer.line(x + spacePerSubdivisionX, bottomSide, x + spacePerSubdivisionX, bottomSide + totalHeight);
//                }
//            }
//            for (float y = startGridY; y < startGridY + totalHeight; y += gridSizeY) {
//                for (int i = 0; i < subdivisions; i++) {
//                    shapeRenderer.line(leftSide, y + spacePerSubdivisionY, leftSide + totalWidth, y + spacePerSubdivisionY);
//                }
//            }
//        }

        return null;
    }

    @Override
    public Color getBackgroundColor () {
        return null;
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
}
