package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.talosvfx.talos.editor.addons.scene.events.PaintSurfaceResize;
import com.talosvfx.talos.editor.addons.scene.widgets.PaintToolsPane;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import com.talosvfx.talos.runtime.scene.components.PaintSurfaceComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;

public class PaintSurfaceGizmo extends Gizmo implements Observer, GameAsset.GameAssetUpdateListener {

    private PaintToolsPane paintToolsPane;
    private final ShapeRenderer shapeRenderer;

    private Color color = new Color(Color.WHITE);

    private Texture brushTexture;

    private FrameBuffer frameBuffer;
    private Batch innerBatch;

    private Vector2 mouseCordsOnScene = new Vector2();

    private Actor previousKeyboardFocus;

    public PaintSurfaceGizmo() {
        if (paintToolsPane == null) {
            paintToolsPane = new PaintToolsPane(this);
        }

        shapeRenderer = new ShapeRenderer();
        innerBatch = new SpriteBatch();
        Notifications.registerObserver(this);
        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.LEFT_BRACKET || keycode == Input.Keys.RIGHT_BRACKET) {
                    paintToolsPane.bracketDown(keycode);
                    destroyBrushTexture();
                    return true;
                }

                return false;
            }

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if (keycode == Input.Keys.LEFT_BRACKET || keycode == Input.Keys.RIGHT_BRACKET) {
                    paintToolsPane.bracketUp(keycode);
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!gameObject.hasComponent(PaintSurfaceComponent.class)) return;
        if (!gameObject.isEditorVisible()) return;

        PaintSurfaceComponent surface = gameObject.getComponent(PaintSurfaceComponent.class);

        Texture resource = surface.getGameResource().getResource();
        if (resource != null && !surface.getGameResource().isBroken()) {
            Vector2 size = surface.size;

            if (brushTexture == null) {
                createBrushTexture();
            }

            color.a = surface.overlay;
            batch.setColor(color);
            batch.draw(resource, getX() - size.x / 2f, getY() - size.y / 2f, size.x, size.y);
            batch.setColor(Color.WHITE);


            if (!selected) return;
            // time to draw the bush
            if (!Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
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

    @EventHandler
    public void onSurfaceSizeChangedEvent(PaintSurfaceResize event) {
        if (event.component != gameObject.getComponent(PaintSurfaceComponent.class)) return;
        createFrameBuffer(true);
    }

    private void drawBrushToBuffer() {
        final PaintSurfaceComponent surface = gameObject.getComponent(PaintSurfaceComponent.class);
        final TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
        final Vector2 surfaceSize = surface.size;
        final Texture resource = surface.getGameResource().getResource();

        // get mouse unit cords
        mouseCordsOnScene.set(viewport.getMouseCordsOnScene());

        // get mouse pixel cords
        final float mouseXPosOnSceneInPixels = (mouseCordsOnScene.x + surfaceSize.x / 2.0f - transformComponent.position.x) * resource.getWidth();
        final float mouseYPosOnSceneInPixels = (mouseCordsOnScene.y - surfaceSize.y / 2.0f - transformComponent.position.y) * resource.getHeight() * -1.0f;

        // start drawing
        frameBuffer.begin();
        innerBatch.begin();

        if (paintToolsPane.getCurrentTool() == PaintToolsPane.Tool.ERASER) {
            Gdx.gl.glBlendEquationSeparate(GL20.GL_FUNC_ADD, GL20.GL_FUNC_REVERSE_SUBTRACT);
            innerBatch.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        } else {
            Gdx.gl.glBlendEquationSeparate(GL20.GL_FUNC_ADD, GL20.GL_FUNC_ADD);
            innerBatch.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        }

        final float brushWidthInPixels = brushTexture.getWidth();
        final float brushHeightInPixels = brushTexture.getHeight();
        final float brushXPosInPixels = mouseXPosOnSceneInPixels / surfaceSize.x - brushWidthInPixels / 2.0f;
        final float brushYPosInPixels = mouseYPosOnSceneInPixels / surfaceSize.y - brushHeightInPixels / 2.0f;

        innerBatch.draw(brushTexture, brushXPosInPixels, brushYPosInPixels, brushWidthInPixels, brushHeightInPixels);
        innerBatch.end();

        // update pixmap
        final Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, resource.getWidth(), resource.getHeight());

        frameBuffer.end();

        updateAssetFromPixmap(pixmap, true);
    }

    private void updateAssetFromPixmap (Pixmap pixmap, boolean updateListeners) {
        final PaintSurfaceComponent surface = gameObject.getComponent(PaintSurfaceComponent.class);
        final GameAsset<Texture> gameAsset = surface.gameAsset;

        if (gameAsset.isBroken()) return;

        final Texture resource = gameAsset.getResource();
        final int width = resource.getWidth();
        final int height = resource.getHeight();

        final TextureData textureData = resource.getTextureData();
        if (textureData instanceof PixmapTextureData) {
            textureData.consumePixmap().dispose();
        }
        resource.dispose();

        final Pixmap newPixmap = new Pixmap(width, height, pixmap.getFormat());
        newPixmap.drawPixmap(pixmap,
                0, 0, pixmap.getWidth(), pixmap.getHeight(),
                0, 0, newPixmap.getWidth(), newPixmap.getHeight()
        );

        final Texture texture = new Texture(newPixmap);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        surface.gameAsset.setResourcePayload(texture);

        if (updateListeners) {
            surface.gameAsset.listeners.removeValue(this, true);
            surface.gameAsset.setUpdated();
            surface.gameAsset.listeners.add(this);
        }
    }

    @Override
    public void touchDown(float x, float y, int button) {
        SharedResources.stage.setKeyboardFocus(this);

        if (canDraw()) {
            drawBrushToBuffer();
        }
    }

    @Override
    public void touchDragged(float x, float y) {
        if (canDraw()) {
            drawBrushToBuffer();
        }
    }

    private boolean canDraw () {
        return frameBuffer != null && brushTexture != null;
    }

    @Override
    public void setGameObject(GameObjectContainer gameObjectContainer, GameObject gameObject) {
        super.setGameObject(gameObjectContainer, gameObject);
        if (gameObject.hasComponent(PaintSurfaceComponent.class)) {
            PaintSurfaceComponent surfaceComponent = gameObject.getComponent(PaintSurfaceComponent.class);
            surfaceComponent.gameAsset.listeners.add(this);
        }
        createFrameBuffer(false);
    }

    private FrameBuffer createFrameBuffer(boolean updateListeners) {
        if (frameBuffer != null) {
            frameBuffer.dispose();
        }

        PaintSurfaceComponent surface = gameObject.getComponent(PaintSurfaceComponent.class);
        GameAsset<Texture> gameResource = surface.getGameResource();
        if (gameResource.isBroken()) {
            // no texture is assigned to the surface,skip
            return null;
        }
        Texture resource = gameResource.getResource();
        resource.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        final int widthInPixels = resource.getWidth();
        final int heightInPixels = resource.getHeight();

        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, widthInPixels, heightInPixels, false);

        final Viewport viewport = new FitViewport(widthInPixels, heightInPixels);
        viewport.apply(true);

        frameBuffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        innerBatch.begin();
        innerBatch.setProjectionMatrix(viewport.getCamera().combined);
        Gdx.gl20.glDisable(GL20.GL_SCISSOR_TEST);
        innerBatch.draw(resource, 0.0f, 0.0f, widthInPixels, heightInPixels, 0, 0, resource.getWidth(), resource.getHeight(), false, true);
        innerBatch.end();

        final Pixmap fromFrameBuffer = Pixmap.createFromFrameBuffer(0, 0, widthInPixels, heightInPixels);
        frameBuffer.end();

        updateAssetFromPixmap(fromFrameBuffer, updateListeners);
        return frameBuffer;
    }

    public void destroyBrushTexture() {
        if (brushTexture != null) {
            TextureData textureData = brushTexture.getTextureData();
            if (textureData instanceof PixmapTextureData) {
                textureData.consumePixmap().dispose();
            }

            brushTexture.dispose();
            brushTexture = null;
        }
    }

    private void createBrushTexture () {
        int size = paintToolsPane.getSize();
        float opacity = paintToolsPane.getOpacity();
        float hardness = paintToolsPane.getHardness();
        float maxShift = 0.25f;
        float shift = (1f - hardness) * maxShift;

        final int xRadius = (int) (size * (1f + shift));
        final int yRadius = (int) (size * (1f + shift));
        final Color tempColor = new Color();

        final int width = xRadius * 2;
        final int height = yRadius * 2;
        final Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        final int majorAxisLength = Math.max(xRadius, yRadius);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // set center
                tmp.set(xRadius, yRadius);
                // dist from center
                float dstFromCenter = tmp.dst(x, y);
                // map distance to a range of 0 to 1
                float mappedDistanceFromCenter = dstFromCenter / majorAxisLength * 2.0f;

                float point = 1f - shift * 2f;
                float fadeOff;
                if (mappedDistanceFromCenter < point) {
                    fadeOff = 1;
                } else if (mappedDistanceFromCenter > 1f) {
                    fadeOff = 0;
                } else {
                    fadeOff = 1f - (MathUtils.clamp(mappedDistanceFromCenter, point, 1f) - point) * 2f;
                }

                tempColor.set(paintToolsPane.getColor());
                tempColor.a = fadeOff * opacity;

                if (paintToolsPane.getCurrentTool() == PaintToolsPane.Tool.ERASER) {
                    tempColor.a = fadeOff * opacity;
                }

                pixmap.setColor(tempColor);
                pixmap.drawPixel(x, y);
            }
        }

        brushTexture = new Texture(pixmap);
        brushTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    private void drawBrushPoint(float x, float y, int sizePixels, float hardness) {
        PaintSurfaceComponent surface = gameObject.getComponent(PaintSurfaceComponent.class);
        Texture resource = surface.getGameResource().getResource();
        float xMul = surface.size.x / resource.getWidth();
        float yMul = surface.size.y / resource.getHeight();

        float maxShift = 0.25f;
        float shift = (1f - hardness) * maxShift;

        float xRadius = sizePixels * xMul * (1f + shift);
        float yRadius = sizePixels * yMul * (1f + shift);
        shapeRenderer.ellipse(x - xRadius / 2f, y - yRadius / 2f, xRadius, yRadius, 60);
        xRadius = sizePixels * xMul * (1f - shift);
        yRadius = sizePixels * yMul * (1f - shift);
        shapeRenderer.ellipse(x - xRadius / 2f, y - yRadius / 2f, xRadius, yRadius, 60);
    }

    @Override
    public boolean hit(float x, float y) {
        return selected;
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);

        if (selected) {
            viewport.addActor(paintToolsPane);
            paintToolsPane.setFrom(gameObject);

            viewport.panRequiresSpace(true);
            previousKeyboardFocus = SharedResources.stage.getKeyboardFocus();
            SharedResources.stage.setKeyboardFocus(this);
        } else {
            paintToolsPane.remove();
            viewport.panRequiresSpace(false);
            SharedResources.stage.setKeyboardFocus(previousKeyboardFocus);
        }
    }

    @Override
    public void notifyRemove() {
        viewport.panRequiresSpace(false);
        paintToolsPane.remove();
        if (gameObject.hasComponent(PaintSurfaceComponent.class)) {
            PaintSurfaceComponent surfaceComponent = gameObject.getComponent(PaintSurfaceComponent.class);
            surfaceComponent.gameAsset.listeners.removeValue(this, true);
        }
        Notifications.unregisterObserver(this);
    }

    @Override
    public void onUpdate() {
        createFrameBuffer(false);
    }
}
