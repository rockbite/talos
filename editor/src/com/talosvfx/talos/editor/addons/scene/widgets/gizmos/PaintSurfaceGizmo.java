package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.logic.components.PaintSurfaceComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.widgets.PaintToolsPane;
import com.talosvfx.talos.editor.project2.SharedResources;

public class PaintSurfaceGizmo extends Gizmo {

    private PaintToolsPane paintToolsPane;
    private final ShapeRenderer shapeRenderer;

    private Color color = new Color(Color.WHITE);

    public Texture brushTexture;

    private FrameBuffer frameBuffer;
    private Batch innerBatch;

    private Vector2 mouseCordsOnScene = new Vector2();

    public PaintSurfaceGizmo() {
        if (paintToolsPane == null) {
            paintToolsPane = new PaintToolsPane(this);
        }

        shapeRenderer = new ShapeRenderer();

        innerBatch = new SpriteBatch();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(gameObject.hasComponent(PaintSurfaceComponent.class) && selected) {
            PaintSurfaceComponent surface = gameObject.getComponent(PaintSurfaceComponent.class);

            Texture resource = surface.getGameResource().getResource();
            if(resource != null) {
                float overlay = surface.overlay;
                Vector2 size = surface.size;
                color.a = overlay;
                batch.setColor(color);
                batch.draw(resource, getX() - size.x / 2f, getY() - size.y / 2f, size.x, size.y);
                batch.setColor(Color.WHITE);

                // time to draw the bush
                if(!Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                    mouseCordsOnScene.set(viewport.getMouseCordsOnScene());

                    batch.end();
                    color.set(Color.WHITE);
                    color.a = 0.1f;
                    shapeRenderer.setColor(color);
                    shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
                    Gdx.gl.glEnable(GL20.GL_BLEND);
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                    drawBrushPoint(mouseCordsOnScene.x, mouseCordsOnScene.y, paintToolsPane.getSize(), paintToolsPane.getHardness());
                    shapeRenderer.end();
                    Gdx.gl.glDisable(GL20.GL_BLEND);
                    batch.begin();
                }
            }
        }
    }

    @Override
    public void keyDown(InputEvent event, int keycode) {
        if(keycode == Input.Keys.LEFT_BRACKET || keycode == Input.Keys.RIGHT_BRACKET) {
            paintToolsPane.bracketDown(keycode);
            brushTexture = null;
        }
    }

    @Override
    public void keyUp(InputEvent event, int keycode) {
        if(keycode == Input.Keys.LEFT_BRACKET || keycode == Input.Keys.RIGHT_BRACKET) {
            paintToolsPane.bracketUp(keycode);
        }
    }

    private void drawBrushToBuffer() {

        PaintSurfaceComponent surface = gameObject.getComponent(PaintSurfaceComponent.class);
        TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
        // figure out mouse pos on the texture

        mouseCordsOnScene.set(viewport.getMouseCordsOnScene());

        tmp.set(transformComponent.position).sub(surface.size.x/2f, surface.size.y/2f).sub(mouseCordsOnScene).scl(-1);

        frameBuffer.begin();
        innerBatch.begin();

        if(paintToolsPane.getCurrentTool() == PaintToolsPane.Tool.ERASER) {
            Gdx.gl.glBlendEquationSeparate(GL20.GL_FUNC_ADD, GL20.GL_FUNC_REVERSE_SUBTRACT);
            innerBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        } else {
            Gdx.gl.glBlendEquationSeparate(GL20.GL_FUNC_ADD, GL20.GL_FUNC_ADD);
            innerBatch.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        }

        innerBatch.draw(brushTexture,
                tmp.x - brushTexture.getWidth()/2f, 400 - (tmp.y-brushTexture.getHeight()/2f) - brushTexture.getHeight(), brushTexture.getWidth(), brushTexture.getHeight());

        innerBatch.end();

        Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, frameBuffer.getWidth(), frameBuffer.getHeight());

        frameBuffer.end();
        String path = surface.gameAsset.dependentRawAssets.first().handle.path();
        FileHandle handle = Gdx.files.absolute(path);

        PixmapIO.writePNG(handle, pixmap);
        pixmap.dispose();
        Texture texture = new Texture(handle);

        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        surface.gameAsset.setResourcePayload(texture);
        surface.gameAsset.setUpdated();
    }

    @Override
    public void touchDown(float x, float y, int button) {
        SharedResources.stage.setKeyboardFocus(this);

        if(brushTexture == null) {
            createBrushTexture();
        }
        boolean sizeIsDifferent = false;

        PaintSurfaceComponent surface = gameObject.getComponent(PaintSurfaceComponent.class);
        Texture resource = surface.getGameResource().getResource();
        if(frameBuffer != null) {
            if(frameBuffer.getWidth() != resource.getWidth() || frameBuffer.getHeight() != resource.getHeight()) {
                sizeIsDifferent = true;
            }
        }

        if(frameBuffer == null || sizeIsDifferent) {
            createFrameBuffer();
        }

        drawBrushToBuffer();
    }

    @Override
    public void touchDragged(float x, float y) {
        drawBrushToBuffer();
    }

    @Override
    public void touchUp(float x, float y) {

    }

    private void createFrameBuffer() {
        PaintSurfaceComponent surface = gameObject.getComponent(PaintSurfaceComponent.class);
        Texture resource = surface.getGameResource().getResource();
        resource.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, resource.getWidth(), resource.getHeight(), false);

        Viewport viewport = new FitViewport(resource.getWidth(), resource.getHeight());
        viewport.setWorldSize(resource.getWidth(), resource.getHeight());
        viewport.apply(true);


        frameBuffer.begin();
        innerBatch.disableBlending();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        Gdx.gl.glClearColor(1, 1, 1, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        innerBatch.setProjectionMatrix(viewport.getCamera().combined);
        innerBatch.begin();
        innerBatch.draw(resource, 0, 0f, resource.getWidth(), resource.getHeight(),
                0, 0, resource.getWidth(), resource.getHeight(),
                false, true);
        innerBatch.end();
        frameBuffer.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        innerBatch.enableBlending();
    }

    private void createBrushTexture() {
        int size = paintToolsPane.getSize();
        float opacity = paintToolsPane.getOpacity();
        float hardness = paintToolsPane.getHardness();
        float maxShift = 0.25f;
        float shift = (1f - hardness) * maxShift;
        int boxSize = (int) (size * (1f+shift));

        Pixmap pixmap = new Pixmap(boxSize, boxSize, Pixmap.Format.RGBA8888);
        for(int x = 0; x < pixmap.getWidth(); x++) {
            for(int y = 0; y < pixmap.getHeight(); y++) {
                color.set(paintToolsPane.getColor());
                float dstFromCenter = (tmp.set(boxSize/2f, boxSize/2f).dst(x+0.5f, y+0.5f))/(boxSize/2f);
                float point = 1f - shift * 2f;
                float fadeOff;
                if(dstFromCenter < point) {
                    fadeOff = 1;
                } else if(dstFromCenter > 1f) {
                    fadeOff = 0;
                } else {
                    fadeOff = 1f - (MathUtils.clamp(dstFromCenter, point, 1f) - point) * 2f;
                }

                color.a = fadeOff * opacity;

                if(paintToolsPane.getCurrentTool() == PaintToolsPane.Tool.ERASER) {
                    color.a = fadeOff * opacity;
                }

                pixmap.setColor(color);
                pixmap.drawPixel(x, y);
            }
        }

       brushTexture = new Texture(pixmap);
       brushTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    private void drawBrushPoint(float x, float y, int sizePixels, float hardness) {
        PaintSurfaceComponent surface = gameObject.getComponent(PaintSurfaceComponent.class);
        Texture resource = surface.getGameResource().getResource();
        float xMul = surface.size.x  / resource.getWidth();
        float yMul = surface.size.y  / resource.getHeight();

        float maxShift = 0.25f;
        float shift = (1f - hardness) * maxShift;

        float xRadius = sizePixels * xMul * (1f + shift);
        float yRadius = sizePixels * yMul * (1f + shift);
        shapeRenderer.ellipse(x - xRadius/2f, y - yRadius/2f, xRadius, yRadius, 60);
        xRadius = sizePixels * xMul * (1f - shift);
        yRadius = sizePixels * yMul * (1f - shift);
        shapeRenderer.ellipse(x - xRadius/2f, y - yRadius/2f, xRadius, yRadius, 60);
    }

    @Override
    public boolean hit(float x, float y) {
        if (!selected) return false;

        return true;
    }

    @Override
    public void mouseMoved(float x, float y) {
        super.mouseMoved(x, y);
    }

    @Override
    public void setSelected (boolean selected) {
        super.setSelected(selected);

        if (selected) {
            viewport.addActor(paintToolsPane);
            paintToolsPane.setFrom(gameObject);

            viewport.panRequiresSpace(true);
        } else {
            paintToolsPane.remove();
            viewport.panRequiresSpace(false);
        }
    }

    @Override
    public void notifyRemove () {
        viewport.panRequiresSpace(false);
        paintToolsPane.remove();
    }
}
