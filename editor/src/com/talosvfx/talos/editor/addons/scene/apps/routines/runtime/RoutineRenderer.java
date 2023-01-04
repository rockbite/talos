package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.draw.DrawableQuad;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes.RenderRoutineNode;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.GameObjectContainer;
import com.talosvfx.talos.editor.addons.scene.logic.SavableContainer;
import com.talosvfx.talos.editor.addons.scene.logic.components.RoutineRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers.PropertyWrapper;

import java.util.Comparator;

public class RoutineRenderer {

    private Comparator<? super DrawableQuad> zComparator;

    private float renderCoolDown = 0f;
    private TextureRegion textureRegion = new TextureRegion();


    private static ObjectMap<Texture, NinePatch> patchCache = new ObjectMap<>();

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
        boolean configured = routineInstance.checkConfigured();

        Vector2 viewportSize = routineRendererComponent.viewportSize;

        TransformComponent transform = gameObject.getComponent(TransformComponent.class);

        renderCoolDown -= Gdx.graphics.getDeltaTime();

        boolean reset = false;

        if(renderCoolDown <= 0f && routineInstance.isDirty()) {
            renderCoolDown = routineRendererComponent.cacheCoolDown;
            reset = true;
        }

        float cameraCull = 4;

        RoutineNode main = routineInstance.getNodeById("main");
        if (main instanceof RenderRoutineNode) {
            RenderRoutineNode renderRoutineNode = (RenderRoutineNode) main;

            OrthographicCamera camera = (OrthographicCamera) mainRenderer.getCamera();
            cameraViewportRect.setSize(camera.viewportWidth * camera.zoom + cameraCull, camera.viewportHeight * camera.zoom + cameraCull).setCenter(camera.position.x, camera.position.y);

            //todo: instead make a nice node to do it
            objectViewportRect.setSize(viewportSize.x, viewportSize.y).setCenter(transform.position);
            /*

            Intersector.intersectRectangles(cameraViewportRect, objectViewportRect, intersectionRect);

            intersectionRect.getCenter(positionTemp);
            intersectionRect.getSize(sizeTemp);
            if (!renderRoutineNode.viewportPosition.equals(positionTemp) || !renderRoutineNode.viewportSize.equals(sizeTemp)) {
                renderRoutineNode.viewportPosition.set(positionTemp);
                renderRoutineNode.viewportSize.set(sizeTemp);
                reset = true;
            }*/

            renderRoutineNode.position.set(transform.position);
            renderRoutineNode.size.set(routineRendererComponent.viewportSize);
            renderRoutineNode.viewportPosition.set(camera.position.x, camera.position.y);
            renderRoutineNode.viewportSize.set(cameraViewportRect.width, cameraViewportRect.height);

            if(routineRendererComponent.cacheCoolDown == 0) {
                reset  = true;
            }

            if(routineInstance.isDirty() && routineInstance.drawableQuads.size == 0) {
                reset = true;
            }

            if(!configured) {
                reset = false;
            }

            if(reset) {
                routineInstance.clearMemory();
                routineInstance.getProperties().clear();

                for (PropertyWrapper<?> propertyWrapper : routineRendererComponent.propertyWrappers) {
                    routineInstance.getProperties().put(propertyWrapper.propertyName, propertyWrapper);
                }

                routineInstance.setContainer(findContainer(gameObject.parent));
                renderRoutineNode.receiveSignal("startSignal");
                routineInstance.drawableQuads.sort(zComparator);
                routineInstance.setDirty(false);
            }

            for (DrawableQuad drawableQuad : routineInstance.drawableQuads) {
                if(drawableQuad.renderMode == SpriteRendererComponent.RenderMode.sliced) {
                    drawSliced(batch, drawableQuad);
                } else {
                    boolean aspect = drawableQuad.aspect;
                    float scl = (float) drawableQuad.texture.getWidth() / drawableQuad.texture.getHeight();
                    float width = drawableQuad.size.x;
                    float height = drawableQuad.size.y;
                    if (aspect) {
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

    private SavableContainer findContainer(GameObject go) {
        GameObject root = findRootGO(go);
        GameObjectContainer container = root.getGameObjectContainerRoot();
        if(container instanceof SavableContainer) {
            return (SavableContainer) container;
        }
        return null;

    }

    private GameObject findRootGO(GameObject go) {
        if(go.getParent() != null) {
            return findRootGO(go.getParent());
        }
        return go;

    }

    private void drawSliced(Batch batch, DrawableQuad drawableQuad) {
        SpriteMetadata metadata = drawableQuad.metadata;
        Texture texture = drawableQuad.texture;
        NinePatch patch = obtainNinePatch(texture, metadata);// todo: this has to be done better

        float width = drawableQuad.size.x;
        float height = drawableQuad.size.y;

        float xSign = width < 0 ? -1 : 1;
        float ySign = height < 0 ? -1 : 1;

        float pivotX = 0.5f;
        float pivotY = 0.5f;

        batch.setColor(drawableQuad.color);
        patch.draw(batch,
                drawableQuad.position.x, drawableQuad.position.y,
                pivotX * width * xSign, pivotY * height * ySign,
                Math.abs(width), Math.abs(height),
                xSign, ySign,
                drawableQuad.rotation);
        batch.setColor(Color.WHITE);
    }

    private NinePatch obtainNinePatch (Texture texture, SpriteMetadata metadata) {
        if(patchCache.containsKey(texture)) { //something better, maybe hash on pixel size + texture for this
            return patchCache.get(texture);
        } else {
            NinePatch patch = new NinePatch(texture, metadata.borderData[0], metadata.borderData[1], metadata.borderData[2], metadata.borderData[3]);
            patch.scale(1/metadata.pixelsPerUnit, 1/metadata.pixelsPerUnit); // fix this later
            patchCache.put(texture, patch);
            return patch;
        }
    }
}
