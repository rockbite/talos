package com.talosvfx.talos.editor.utils.grid.property_providers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.maps.TalosLayer;
import com.talosvfx.talos.editor.utils.grid.GridLine;

public class StaticBoundedGridPropertyProvider extends StaticGridPropertyProvider {

    @Override
    public void update (OrthographicCamera camera, float alpha) {
        this.camera = camera;
        gridLines.clear();
        float gridSizeX = getUnitX();
        float gridSizeY = getUnitY();

        float totalWidth = getWorldWidth();
        float totalHeight = getWorldHeight();

        Color color = Color.valueOf("7070704D");
        Color zeroColor = Color.CYAN;
        zeroColor.a = 0.4f;

        float a = color.a;
        a *= 1 / camera.zoom;
        color.a = MathUtils.clamp(a, 0.05f, 0.3f);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        startY = -totalHeight / 2;
        startX = -totalWidth / 2;


        for (float x = startX; x < totalWidth / 2; x += gridSizeX) {
            gridLines.add(new GridLine(new Vector2(x, -totalHeight / 2), new Vector2(x, totalHeight / 2), (x == 0 && shouldHighlightZero()) ? zeroColor : color, thickness));
            endX = x;
        }

        for (float y = startY; y < totalHeight / 2; y += gridSizeY) {
            gridLines.add(new GridLine(new Vector2(-totalWidth / 2, y), new Vector2(totalWidth / 2, y), (y == 0 && shouldHighlightZero()) ? zeroColor : color, thickness));
            endY = y;
        }
    }

    @Override
    public float getWorldHeight () {
        if (SceneEditorWorkspace.getInstance().mapEditorState.isEditing()) {
            TalosLayer selectedLayer = SceneEditorWorkspace.getInstance().mapEditorState.getLayerSelected();
            if (selectedLayer == null) {
                return -1;
            } else {
                return selectedLayer.getMapHeight();
            }
        }

        return -1;
    }

    @Override
    public float getWorldWidth () {
        if (SceneEditorWorkspace.getInstance().mapEditorState.isEditing()) {
            TalosLayer selectedLayer = SceneEditorWorkspace.getInstance().mapEditorState.getLayerSelected();

            if (selectedLayer == null) {
                return -1;
            } else {
                return selectedLayer.getMapWidth();
            }
        }

        return -1;
    }
}
