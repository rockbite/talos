package com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.nodes;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.draw.DrawableQuad;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;

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
        Texture resource = asset.getResource();
        drawableQuad.texture = resource;

        drawableQuad.rotation = fetchFloatValue("rotation");
        drawableQuad.color = fetchColorValue("color");
        if(drawableQuad.color == null) {
            drawableQuad.color = Color.WHITE;
        }
        drawableQuad.aspect = fetchBooleanValue("aspect");

        routineInstanceRef.drawableQuads.add(drawableQuad);
    }
}
