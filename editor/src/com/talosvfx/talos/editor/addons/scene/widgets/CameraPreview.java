package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.CameraComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;

public class CameraPreview extends Actor {

    private FrameBuffer frameBuffer;
    private PolygonSpriteBatch polygonSpriteBatch;
    private Viewport viewport;

    TextureRegion white;

    private Vector2 pixelSize = new Vector2();
    private Vector2 worldSize = new Vector2();
    private CameraComponent component;
    private GameObject cameraObject;

    private OrthographicCamera camera;

    private float presumedRotation = 0;

    public CameraPreview () {
        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, 200, 200, false);
        polygonSpriteBatch = new PolygonSpriteBatch();
        viewport = new FitViewport(10, 10);
        white = TalosMain.Instance().getSkin().getRegion("white");
        camera = new OrthographicCamera();
    }

    @Override
    public void act (float delta) {
        super.act(delta);
        TransformComponent transform = cameraObject.getComponent(TransformComponent.class);
        viewport.getCamera().position.set(transform.position.x, transform.position.y, 0);
        ((OrthographicCamera)viewport.getCamera()).zoom = component.zoom;
        OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
        float currRotation = getCameraAngle(camera);
        if(currRotation == 360) currRotation = 0;
        if(currRotation != presumedRotation) {
            presumedRotation = currRotation;
        }

        float nextRotation = transform.rotation;

        // some weird fuckery I had to do because camera just does not have setRotation, fun. sure it does not.
        if(nextRotation != presumedRotation && Math.abs(nextRotation - presumedRotation) > 0.001f) {
            float diff = nextRotation - presumedRotation;
            camera.rotate(diff);
            presumedRotation = nextRotation;
        }

    }

    public float getCameraAngle(OrthographicCamera cam) {
        return ((float) Math.atan2(cam.up.x, cam.up.y) * MathUtils.radiansToDegrees);
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        if(component == null || cameraObject == null) return;

        batch.end();

        viewport.setWorldSize(worldSize.x, worldSize.y);
        viewport.apply(false);

        frameBuffer.begin();
        Gdx.gl.glClearColor(component.backgroundColor.r, component.backgroundColor.g, component.backgroundColor.b, component.backgroundColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        polygonSpriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        polygonSpriteBatch.begin();

        // draw game preview here
        drawPreview();

        polygonSpriteBatch.end();
        frameBuffer.end();

        batch.begin();

        Texture colorBufferTexture = frameBuffer.getColorBufferTexture();
        float u = (1f - (pixelSize.x/200f))/2f;
        float v = (1f - (pixelSize.y/200f))/2f;
        float u2 = 1f - u;
        float v2 = 1f - v;
        batch.draw(colorBufferTexture, getX() , getY(), getWidth(), getHeight(), 0, 0, 1,1);
    }

    private void drawPreview () {


        SceneEditorWorkspace workspace = SceneEditorAddon.get().workspace;
        GameObject rootGO = workspace.getRootGO();
        MainRenderer renderer = SceneEditorAddon.get().workspace.getRenderer();

        renderer.skipUpdates = true;

        CameraComponent cameraObjectComponent = cameraObject.getComponent(CameraComponent.class);
        TransformComponent transformComponent = cameraObject.getComponent(TransformComponent.class);

        camera.zoom = cameraObjectComponent.zoom;
        camera.viewportWidth = cameraObjectComponent.size.x;
        camera.viewportHeight = cameraObjectComponent.size.y;
        Vector2 position = transformComponent.position;
        camera.position.set(position.x, position.y, 0);

        renderer.setCamera(camera);
        renderer.update(rootGO);
        renderer.render(polygonSpriteBatch, new MainRenderer.RenderState(), rootGO);

        renderer.skipUpdates = false;
    }

    public void setViewport (float worldWidth, float worldHeight, float width, float height) {
        worldSize.set(worldWidth, worldHeight);
        pixelSize.set(width, height);
    }

    public void setCamera (GameObject cameraObject) {
        this.cameraObject = cameraObject;
        this.component = cameraObject.getComponent(CameraComponent.class);
    }
}
