package com.talosvfx.talos.editor.utils.grid.property_providers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.editor.utils.grid.GridLine;

public class StaticBoundedGridPropertyProvider extends StaticGridPropertyProvider {

    @Override
    public void update (OrthographicCamera camera, float alpha) {
        this.camera = camera;
        gridLines.clear();
        float gridSizeX = getUnitX();
        float gridSizeY = getUnitY();

        float totalWidth = getWorldWidth();
        float totalHeight = getWorldWidth();

        Color color = Color.valueOf("7070704D");
        Color zeroColor = Color.CYAN;
        zeroColor.a = 0.4f;

        float a = color.a;
        a *= 1 / camera.zoom;
        color.a = MathUtils.clamp(a, 0.05f, 0.3f);

        Gdx.gl.glEnable(GL20.GL_BLEND);


        for (float x = -totalWidth / 2; x < totalWidth / 2; x += gridSizeX) {
            gridLines.add(new GridLine(new Vector2(x, -totalHeight / 2), new Vector2(x, totalHeight / 2), x == 0 ? zeroColor : color, thickness));
            endX = x;
        }

        for (float y = -totalHeight / 2; y < totalHeight / 2; y += gridSizeY) {
            gridLines.add(new GridLine(new Vector2(-totalWidth / 2, y), new Vector2(totalWidth / 2, y), y == 0 ? zeroColor : color, thickness));
            endY = y;
        }
    }
}
