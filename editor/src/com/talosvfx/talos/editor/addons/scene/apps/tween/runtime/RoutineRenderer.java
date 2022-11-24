package com.talosvfx.talos.editor.addons.scene.apps.tween.runtime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.draw.DrawableQuad;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.nodes.RenderRoutineNode;
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


    Rectangle mainCameraRect = new Rectangle();
    Rectangle viewportRect = new Rectangle();
    Rectangle intersectionRect = new Rectangle();
    Vector2 positionTempVector = new Vector2();
    Vector2 sizeTempVector = new Vector2();

    public void render(MainRenderer mainRenderer, Batch batch, GameObject gameObject, RoutineRendererComponent routineRendererComponent) {
        RoutineInstance routineInstance = routineRendererComponent.routineInstance;
        Vector2 viewportSize = routineRendererComponent.viewportSize;

        TransformComponent transform = gameObject.getComponent(TransformComponent.class);

        renderCoolDown -= Gdx.graphics.getDeltaTime();

        boolean reset = false;

        if(renderCoolDown <= 0f && routineInstance.isDirty) {
            renderCoolDown = 0.1f;
            reset = true;
        }

        float offset = 5;

        RoutineNode main = routineInstance.getNodeById("main");
        if (main instanceof RenderRoutineNode) {
            RenderRoutineNode renderRoutineNode = (RenderRoutineNode) main;

            OrthographicCamera camera = mainRenderer.getCamera();
            Vector3 cameraPos = camera.position;
            mainCameraRect.setSize(camera.viewportWidth * camera.zoom + offset, camera.viewportHeight * camera.zoom + offset).setCenter(cameraPos.x, cameraPos.y);

            Vector2 position = transform.position;
            viewportRect.setSize(viewportSize.x, viewportSize.y).setCenter(position.x, position.y);

            boolean intersects = Intersector.intersectRectangles(mainCameraRect, viewportRect, intersectionRect);

            intersectionRect.getCenter(positionTempVector);
            intersectionRect.getSize(sizeTempVector);


            if (!(positionTempVector.equals(renderRoutineNode.position) && sizeTempVector.equals(renderRoutineNode.viewportSize))) {
                reset = true;
            }

            if (reset) {
                routineInstance.clearMemory();
                renderRoutineNode.position.set(positionTempVector);
                renderRoutineNode.viewportSize.set(sizeTempVector);
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
                        1f, 1f,
                        width, height,
                        drawableQuad.rotation);
                batch.setColor(Color.WHITE);
            }
        }
    }
}
