package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.runtime.assets.AMetadata;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.RawAsset;
import com.talosvfx.talos.runtime.assets.meta.SpriteMetadata;
import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.routine.draw.DrawableQuad;
import com.talosvfx.talos.runtime.scene.components.SpriteRendererComponent;

public class RenderQuadNode extends RoutineNode {


    @Override
    public void receiveSignal(String portName) {

        DrawableQuad drawableQuad = Pools.obtain(DrawableQuad.class);
        float x = fetchFloatValue("x");
        float y = fetchFloatValue("y");
        float z = fetchFloatValue("z");
        float width = fetchFloatValue("width");
        float height = fetchFloatValue("height");
        drawableQuad.position.set(x, y);
        drawableQuad.z = z;
        drawableQuad.size.set(width, height);

        GameAsset<Texture> asset = fetchAssetValue("sprite");

        if (asset == null || asset.getRootRawAsset() == null) {
            return;
        }

        RawAsset rawAsset = asset.getRootRawAsset();
        AMetadata metaData = rawAsset.metaData;
        if (metaData instanceof SpriteMetadata) {
            SpriteMetadata sData = (SpriteMetadata) metaData;
            if(sData.borderData != null) {
                // this is a nine patch;
                drawableQuad.metadata = sData;
            }
        }

        Texture resource = asset.getResource();
        if (resource == null) {
            return;
        }
        drawableQuad.gameAsset = asset;

        drawableQuad.rotation = fetchFloatValue("rotation");
        drawableQuad.color.set(fetchColorValue("color"));
        if(drawableQuad.color == null) {
            drawableQuad.color = Color.WHITE;
        }
        drawableQuad.aspect = fetchBooleanValue("aspect");
        String mode = fetchStringValue("mode");
        if(mode == null) mode = "simple";
        drawableQuad.renderMode = SpriteRendererComponent.RenderMode.simple.valueOf(mode);

        routineInstanceRef.drawableQuads.add(drawableQuad);
    }
}
