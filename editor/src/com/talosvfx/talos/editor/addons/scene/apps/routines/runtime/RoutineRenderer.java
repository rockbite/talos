package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.draw.DrawableQuad;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes.RenderRoutineNode;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.RoutineRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;

import java.util.Comparator;

public class RoutineRenderer {

    private Comparator<? super DrawableQuad> zComparator;

    private float renderCoolDown = 0f;
    private TextureRegion textureRegion = new TextureRegion();


    public RoutineRenderer () {
        zComparator = new Comparator<DrawableQuad>() {
            @Override
            public int compare(DrawableQuad o2, DrawableQuad o1) {
                int result = (int) (o1.z*10000 - o2.z*10000);
                if(result == 0) {
                    result = (int) (o1.position.x*10000 - o2.position.x*10000);
                }
                if(result == 0) {
                    result = (int) (o1.position.y*10000 - o2.position.y*10000);
                }
                return result;
            }
        };
    }


    private final Rectangle cameraViewportRect = new Rectangle();
    private final Rectangle objectViewportRect = new Rectangle();
    private final Rectangle intersectionRect = new Rectangle();
    private final Vector2 positionTemp = new Vector2();
    private final Vector2 sizeTemp = new Vector2();


    public void render(MainRenderer mainRenderer, Batch batch, GameObject gameObject, RoutineRendererComponent routineRendererComponent) {
        RoutineInstance routineInstance = routineRendererComponent.routineInstance;
        Vector2 viewportSize = routineRendererComponent.viewportSize;

        TransformComponent transform = gameObject.getComponent(TransformComponent.class);

        renderCoolDown -= Gdx.graphics.getDeltaTime();

        boolean reset = false;

        if(renderCoolDown <= 0f && routineInstance.isDirty) {
            renderCoolDown = routineRendererComponent.cacheCoolDown;
            reset = true;
        }

        float cameraCull = 4;

        RoutineNode main = routineInstance.getNodeById("main");
        if (main instanceof RenderRoutineNode) {
            RenderRoutineNode renderRoutineNode = (RenderRoutineNode) main;


            OrthographicCamera camera = (OrthographicCamera) mainRenderer.getCamera();
            cameraViewportRect.setSize(camera.viewportWidth * camera.zoom + cameraCull, camera.viewportHeight * camera.zoom + cameraCull).setCenter(camera.position.x, camera.position.y);
            objectViewportRect.setSize(viewportSize.x, viewportSize.y).setCenter(transform.position);
            Intersector.intersectRectangles(cameraViewportRect, objectViewportRect, intersectionRect);

            intersectionRect.getCenter(positionTemp);
            intersectionRect.getSize(sizeTemp);
            if (!renderRoutineNode.viewportPosition.equals(positionTemp) || !renderRoutineNode.viewportSize.equals(sizeTemp)) {
                renderRoutineNode.viewportPosition.set(positionTemp);
                renderRoutineNode.viewportSize.set(sizeTemp);
                reset = true;
            }

            renderRoutineNode.position.set(transform.position);

            if(reset) {
                routineInstance.clearMemory();
                renderRoutineNode.receiveSignal("startSignal");
                routineInstance.drawableQuads.sort(zComparator);
                routineInstance.isDirty = false;
            }

            for (DrawableQuad drawableQuad : routineInstance.drawableQuads) {
                boolean aspect = drawableQuad.aspect;
                float scl = (float)drawableQuad.texture.getWidth() / drawableQuad.texture.getHeight();
                float width = drawableQuad.size.x;
                float height = drawableQuad.size.y;
                if(aspect) {
                    height = width / scl;
                }

                textureRegion.setRegion(drawableQuad.texture);
                batch.setColor(drawableQuad.color);
                batch.draw(textureRegion,
                        drawableQuad.position.x, drawableQuad.position.y,
                        0.5f, 0.5f,
                        width, height,
                        1, 1,
                        drawableQuad.rotation);
                batch.setColor(Color.WHITE);
            }
        }
    }
}
