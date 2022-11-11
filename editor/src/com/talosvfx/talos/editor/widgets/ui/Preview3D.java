package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.rockbite.bongo.engine.camera.BongoCameraController;
import com.rockbite.bongo.engine.systems.RenderPassSystem;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.wrappers.IDragPointProvider;
import com.talosvfx.talos.runtime.IEmitter;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticleEffectInstance;
import com.talosvfx.talos.runtime.render.Particle3DRenderer;
import com.talosvfx.talos.runtime.render.p3d.Simple3DBatch;
import lombok.Getter;

import static com.rockbite.bongo.engine.systems.RenderPassSystem.glViewport;

public class Preview3D extends PreviewWidget {

    private final TinyGizmoRenderer tinyGizmoRenderer;

    @Getter
    private final BongoPreview bongoPreview;
    //Controls
    private InputAdapter cameraInputController;

    //Render
    public Camera worldCamera;
    private boolean isDrawXYZ, isDrawXZPlane, isDrawXYPlane;
    private Array<Model> models;
    private ModelInstance xyzInstance, xzPlaneInstance, xyPlaneInstance;
    private Environment environment;

    private IDragPointProvider dragPointProvider;
    private Array<DragPoint> dragPoints = new Array<>();


    public Preview3D(PreviewImageControllerWidget previewImageControllerWidget) {
        super(previewImageControllerWidget);
        cameraController.scrollOnly = true;

        environment = new Environment();
        environment.add(new DirectionalLight().set(Color.WHITE, 0,0,-1));


        models = new Array<Model>();
        ModelBuilder builder = new ModelBuilder();
        Model 	xyzModel = builder.createXYZCoordinates(10, new Material(), VertexAttributes.Usage.Position| VertexAttributes.Usage.ColorPacked),
                planeModel = builder.createLineGrid(10, 10, 1, 1, new Material(ColorAttribute.createDiffuse(Color.WHITE)), VertexAttributes.Usage.Position);
        models.add(xyzModel);
        models.add(planeModel);
        xyzInstance = new ModelInstance(xyzModel);
        xzPlaneInstance = new ModelInstance(planeModel);
        xyPlaneInstance = new ModelInstance(planeModel);
        xyPlaneInstance.transform.rotate(1f, 0f, 0f, 90f);

        setDrawXYZ(true);
        setDrawXZPlane(true);

        tinyGizmoRenderer = new TinyGizmoRenderer();

        bongoPreview = new BongoPreview();
        worldCamera = bongoPreview.getWorldCamera();
        worldCamera.position.set(8, 5, 8);
        worldCamera.lookAt(0, 2, 0);
        worldCamera.near = 0.1f;
        worldCamera.far = 100f;
        worldCamera.update();

        cameraInputController = new BongoCameraController(worldCamera);
        if (cameraInputController instanceof BongoCameraController) {
            ((BongoCameraController)cameraInputController).translateTarget = false;
        }

        bongoPreview.setCameraController(cameraInputController);


    }

    @Override
    protected void addPanListener() {
        addListener(new InputListener() {
            @Override
            public boolean keyDown (InputEvent event, int keycode) {
                tinyGizmoRenderer.getInputAdapter().keyDown(keycode);

                return super.keyDown(event, keycode);
            }

            @Override
            public boolean keyUp (InputEvent event, int keycode) {
                tinyGizmoRenderer.getInputAdapter().keyUp(keycode);
                return super.keyUp(event, keycode);
            }

            @Override
            public boolean scrolled (InputEvent event, float x, float y, float amountX, float amountY) {
                final boolean interacted = tinyGizmoRenderer.getInteracted();
                if (interacted) return true;
                if (cameraInputController.scrolled(amountX, amountY)) return true;
                return super.scrolled(event, x, y, amountX, amountY);
            }

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                tinyGizmoRenderer.getInputAdapter().touchDown((int)x, (int)y, pointer, button);
                final boolean interacted = tinyGizmoRenderer.getInteracted();
                if (interacted) return true;
                cameraInputController.touchDown((int)x, Gdx.graphics.getHeight() - (int)y, pointer, button);
                return !event.isHandled();
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                tinyGizmoRenderer.getInputAdapter().touchUp((int)x, (int)y, pointer, button);
                final boolean interacted = tinyGizmoRenderer.getInteracted();
                if (interacted) return;
                cameraInputController.touchUp((int)x, Gdx.graphics.getHeight() - (int)y, pointer, button);
            }

            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                tinyGizmoRenderer.getInputAdapter().touchDragged((int)x, (int)y, pointer);
                final boolean interacted = tinyGizmoRenderer.getInteracted();
                if (interacted) return;
                cameraInputController.touchDragged((int)x, Gdx.graphics.getHeight() - (int)y, pointer);
            }

            @Override
            public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                TalosMain.Instance().UIStage().getStage().setScrollFocus(Preview3D.this);
                TalosMain.Instance().UIStage().getStage().setKeyboardFocus(Preview3D.this);
            }

            @Override
            public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                if (pointer != -1) return; //Only care about exit/enter from mouse move
                TalosMain.Instance().UIStage().getStage().setScrollFocus(null);
                TalosMain.Instance().UIStage().getStage().setKeyboardFocus(null);
            }
        });
    }

    @Override
    public void resetToDefaults() {

    }

    public void setDrawXYZ(boolean isDraw)
    {
        isDrawXYZ = isDraw;
    }

    public boolean IsDrawXYZ()
    {
        return isDrawXYZ;
    }

    public void setDrawXZPlane(boolean isDraw)
    {
        isDrawXZPlane = isDraw;
    }

    public boolean IsDrawXZPlane()
    {
        return isDrawXZPlane;
    }

    public void setDrawXYPlane(boolean isDraw)
    {
        isDrawXYPlane = isDraw;
    }

    public boolean IsDrawXYPlane()
    {
        return isDrawXYPlane;
    }


    @Override
    public void act(float delta) {
        super.act(delta);

//        worldCamera.viewportWidth = getWidth();
//        worldCamera.viewportHeight = getHeight();
//        worldCamera.update();



        if (!tinyGizmoRenderer.getInteracted() && Gdx.input.isTouched()) {
            ((BongoCameraController)cameraInputController).update();
        }
    }

    @Override
    public void drawContent(PolygonBatch batch, float parentAlpha) {
        super.drawContent(batch, parentAlpha);

        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            switchCamera();
        }

        final ParticleEffectInstance particleEffect = TalosMain.Instance().TalosProject().getParticleEffect();
        if (particleEffect != null) {
            bongoPreview.updateParticleInstance(particleEffect);
        }

        batch.end();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);

//        modelBatch.begin(worldCamera);
//        //if(isDrawXYZ) modelBatch.render(xyzInstance);
//        if(isDrawXZPlane) modelBatch.render(xzPlaneInstance);
//        if(isDrawXYPlane) modelBatch.render(xyPlaneInstance);
//
//        //Draw
//        modelBatch.end();


//        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
//        Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);

//        Gdx.gl.glEnable(GL20.GL_BLEND);
//        Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);

//        final ParticleEffectInstance particleEffect = TalosMain.Instance().TalosProject().getParticleEffect();
//        simple3DBatch.begin(worldCamera, shaderProgram);
//        particleRenderer.setBatch(simple3DBatch);
//        particleEffect.render(particleRenderer);
//        simple3DBatch.end();

//        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);




        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        Vector2 temp = new Vector2();
        Vector2 temp2 = new Vector2();

        temp.set(0, 0);
        temp2.set(getWidth(), getHeight());

        localToScreenCoordinates(temp);
        localToScreenCoordinates(temp2);

        temp.y = Gdx.graphics.getHeight() - temp.y;
        temp2.y = Gdx.graphics.getHeight() - temp2.y;

        int width = (int)(temp2.x - temp.x);
        int height = (int)(temp2.y - temp.y);
        int x = (int)temp.x;
        int y = (int)temp.y;

        x = HdpiUtils.toBackBufferX(x);
        y = HdpiUtils.toBackBufferY(y);
        width = HdpiUtils.toBackBufferX(width);
        height = HdpiUtils.toBackBufferY(height);

        RenderPassSystem.glViewport.x = x;
        RenderPassSystem.glViewport.y = y;
        RenderPassSystem.glViewport.width = width;
        RenderPassSystem.glViewport.height = height;
        HdpiUtils.glViewport(
            RenderPassSystem.glViewport.x,
            RenderPassSystem.glViewport.y,
            RenderPassSystem.glViewport.width,
            RenderPassSystem.glViewport.height
        );

        if (worldCamera instanceof PerspectiveCamera) {
            worldCamera.viewportWidth = glViewport.width;
            worldCamera.viewportHeight = glViewport.height;
            worldCamera.update();
        }


        bongoPreview.render();

        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

        tinyGizmoRenderer.render(worldCamera, this, dragPoints);
        for (DragPoint dragPoint : dragPoints) {
            if (dragPoint.changed) {
                dragPointProvider.dragPointChanged(dragPoint);
                dragPoint.changed = false;
            }
        }

        batch.begin();

    }

    boolean isPerspective = true;
    private void switchCamera () {
        final Vector3 position = worldCamera.position;
        final Vector3 direction = worldCamera.direction;

        if (isPerspective) {
            float aspect = (float)Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
            float width = 10f;
            float height = width * aspect;
            OrthographicCamera orthographicCamera = new OrthographicCamera(width, height);
            bongoPreview.setCamera(orthographicCamera);
        } else {
            PerspectiveCamera perspectiveCamera = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            bongoPreview.setCamera(perspectiveCamera);
        }

        isPerspective = !isPerspective;

        worldCamera = bongoPreview.getWorldCamera();
        worldCamera.near = 0.1f;
        worldCamera.far = 100f;
        worldCamera.position.set(position);
        worldCamera.direction.set(direction);
        worldCamera.update();

        if (cameraInputController instanceof CameraInputController) {
            ((CameraInputController)cameraInputController).camera = worldCamera;
        }


    }

    @Override
    public void fileDrop(float x, float y, String[] paths) {

    }

    public void registerForDragPoints(IDragPointProvider dragPointProvider) {
        this.dragPointProvider = dragPointProvider;
        DragPoint[] arr = dragPointProvider.fetchDragPoints();
        dragPoints.clear();
        for(int i = 0; i < arr.length; i++) {
            dragPoints.add(arr[i]);
        }
    }

    public void unregisterDragPoints() {
        this.dragPointProvider = null;
        dragPoints.clear();
    }

    public void unregisterDragPoints(IDragPointProvider dragPointProvider) {
        if(this.dragPointProvider == dragPointProvider) {
            this.dragPointProvider = null;
            dragPoints.clear();
        }
    }

    @Override
    public String getBackgroundImagePath() {
        return null;
    }

    @Override
    public boolean isGridVisible() {
        return false;
    }

    @Override
    public boolean isBackgroundImageInBack() {
        return false;
    }

    @Override
    public float getBgImageSize() {
        return 0;
    }

    @Override
    public float getGridSize() {
        return 0;
    }

    @Override
    public void setBackgroundImage(String bgImagePath) {

    }

    @Override
    public void setGridVisible(boolean isGridVisible) {

    }

    @Override
    public void setImageIsBackground(boolean bgImageIsInBack) {

    }

    @Override
    public void setBgImageSize(float bgImageSize) {

    }

    @Override
    public void setGridSize(float gridSize) {

    }

    @Override
    public void removePreviewImage() {

    }

    @Override
    public void gridSizeChanged(float size) {

    }
}
