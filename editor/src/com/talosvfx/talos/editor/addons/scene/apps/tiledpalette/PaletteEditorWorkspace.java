package com.talosvfx.talos.editor.addons.scene.apps.tiledpalette;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.Scene;
import com.talosvfx.talos.editor.addons.scene.logic.TilePaletteData;
import com.talosvfx.talos.editor.addons.scene.logic.components.MapComponent;
import com.talosvfx.talos.editor.addons.scene.maps.TalosLayer;
import com.talosvfx.talos.editor.utils.GridDrawer;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;

import java.util.UUID;
import java.util.function.Supplier;

public class PaletteEditorWorkspace extends ViewportWidget {
    GameAsset<TilePaletteData> paletteData;


    private GridDrawer gridDrawer;
    private SceneEditorWorkspace.GridProperties gridProperties;

    public PaletteEditorWorkspace(GameAsset<TilePaletteData> paletteData) {
        super();
        this.paletteData = paletteData;
        setWorldSize(10f);
        setCameraPos(0, 0);


        gridProperties = new SceneEditorWorkspace.GridProperties();
        gridProperties.sizeProvider = new Supplier<float[]>() {
            @Override
            public float[] get () {
                if (SceneEditorWorkspace.getInstance().mapEditorState.isEditing()) {
                    TalosLayer selectedLayer = SceneEditorWorkspace.getInstance().mapEditorState.getLayerSelected();

                    if (selectedLayer == null) {
                        return new float[]{1,1};
                    } else {
                        return new float[]{selectedLayer.getTileSizeX(), selectedLayer.getTileSizeY()};
                    }
                }

                return new float[]{1, 1};

            }
        };

        gridDrawer = new GridDrawer(this, camera, gridProperties);
        gridDrawer.highlightCursorHover = true;
    }

    @Override
    public void drawContent(Batch batch, float parentAlpha) {
        batch.end();

        gridDrawer.drawGrid();

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
