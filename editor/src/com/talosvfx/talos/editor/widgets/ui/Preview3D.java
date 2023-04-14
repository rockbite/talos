package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.rockbite.bongo.engine.systems.RenderPassSystem;
import com.talosvfx.talos.editor.wrappers.IDragPointProvider;
import lombok.Getter;

import java.util.function.Supplier;

public class Preview3D extends PreviewWidget {

    @Getter
    private final TinyGizmoRenderer tinyGizmoRenderer;

    @Getter
    private final BongoPreview bongoPreview;

    //Render
    private boolean isDrawXYZ, isDrawXZPlane, isDrawXYPlane;
    private Array<Model> models;
    private ModelInstance xyzInstance, xzPlaneInstance, xyPlaneInstance;
    private Environment environment;

    private IDragPointProvider dragPointProvider;
    private Array<DragPoint> dragPoints = new Array<>();

    public Preview3D() {
        super();
        if (MathUtils.isEqual(viewportViewSettings.getNear(), 0.0f)) {
            viewportViewSettings.setNear(0.01f);
        }
//        cameraController.scrollOnly = true;
        setWorldSize(10);

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

        bongoPreview.setCameraController(viewportViewSettings.getCurrentCameraController());


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

        viewportViewSettings.setDisableCamera(tinyGizmoRenderer.getInteracted() && Gdx.input.isTouched());

        bongoPreview.setCamera(viewportViewSettings.getCurrentCamera());

//        worldCamera.viewportWidth = getWidth();
//        worldCamera.viewportHeight = getHeight();
//        worldCamera.update();


    }

    @Override
    public void drawContent(PolygonBatch batch, float parentAlpha) {
        super.drawContent(batch, parentAlpha);

        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
//            switchCamera();
        }

        if (effectInstance != null) {
            bongoPreview.updateParticleInstance(effectInstance);
        }

        batch.end();


        if (viewportViewSettings.isShowAxis()) {
            drawAxis();
        }

//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
//        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);

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

//        if (worldCamera instanceof PerspectiveCamera) {
//            worldCamera.viewportWidth = glViewport.width;
//            worldCamera.viewportHeight = glViewport.height;
//            worldCamera.update();
//        }


        bongoPreview.render();

        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

        Supplier<Camera> currentCameraSupplier = viewportViewSettings.getCurrentCameraSupplier();
        Camera camera = currentCameraSupplier.get();

        tinyGizmoRenderer.render(camera, this, dragPoints);
        for (DragPoint dragPoint : dragPoints) {
            if (dragPoint.changed) {
                dragPointProvider.dragPointChanged(dragPoint);
                dragPoint.changed = false;
            }
        }

        batch.begin();

    }

    @Override
    protected void updateParticlePosition (Vector3 tmp) {
        super.updateParticlePosition(tmp);
        bongoPreview.updateParticlePosition(tmp);
    }

    boolean isPerspective = true;
    private void switchCamera () {
//        final Vector3 position = worldCamera.position;
//        final Vector3 direction = worldCamera.direction;
//
//        if (isPerspective) {
//            float aspect = (float)Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
//            float width = 10f;
//            float height = width * aspect;
//            OrthographicCamera orthographicCamera = new OrthographicCamera(width, height);
//            bongoPreview.setCamera(orthographicCamera);
//        } else {
//            PerspectiveCamera perspectiveCamera = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//            bongoPreview.setCamera(perspectiveCamera);
//        }
//
//        isPerspective = !isPerspective;
//
//        worldCamera = bongoPreview.getWorldCamera();
//        worldCamera.near = 0.1f;
//        worldCamera.far = 100f;
//        worldCamera.position.set(position);
//        worldCamera.direction.set(direction);
//        worldCamera.update();
//
////        if (cameraInputController instanceof CameraInputController) {
////            ((CameraInputController)cameraInputController).camera = worldCamera;
////        }


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
    public void removePreviewImage() {

    }

    @Override
    public void gridSizeChanged(float size) {

    }
}
