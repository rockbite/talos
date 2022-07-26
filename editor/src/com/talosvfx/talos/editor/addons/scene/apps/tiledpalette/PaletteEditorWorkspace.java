package com.talosvfx.talos.editor.addons.scene.apps.tiledpalette;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.TilePaletteData;
import com.talosvfx.talos.editor.utils.GridDrawer;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;

import java.util.UUID;

public class PaletteEditorWorkspace extends ViewportWidget {
    GameAsset<TilePaletteData> paletteData;

    public PaletteEditorWorkspace(GameAsset<TilePaletteData> paletteData) {
        super();
        this.paletteData = paletteData;
        setWorldSize(10f);
        setCameraPos(0, 0);
    }

    @Override
    public void drawContent(Batch batch, float parentAlpha) {
        batch.end();

        GridDrawer.drawGrid(this, camera, batch, 1, 1, 0, true, true);


        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        ObjectMap<UUID, float[]> positions = paletteData.getResource().positions;
        ObjectMap<UUID, GameAsset<?>> references = paletteData.getResource().references;

        for (ObjectMap.Entry<UUID, GameAsset<?>> reference : references) {
            float[] position = positions.get(reference.key);
            float x = position[0];
            float y = position[1];
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.rect(x, y, 1, 1);
        }
        shapeRenderer.end();

        batch.begin();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        camera.update();
    }
}
