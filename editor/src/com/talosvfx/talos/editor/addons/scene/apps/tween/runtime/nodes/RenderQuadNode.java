package com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.nodes;


import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.draw.DrawableQuad;

public class RenderQuadNode extends RoutineNode {


    @Override
    public void receiveSignal(String portName) {

        DrawableQuad drawableQuad = Pools.obtain(DrawableQuad.class);
        float x = (float)fetchValue("x");
        float y = (float)fetchValue("y");
        float width = (float)fetchValue("width");
        float height = (float)fetchValue("height");
        drawableQuad.position.set(x, y);
        drawableQuad.size.set(width, height);

        routineInstanceRef.drawableQuads.add(drawableQuad);
    }
}
