package com.talosvfx.talos.editor.project2.vfxui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.rockbite.bongo.engine.render.PolygonSpriteBatchMultiTextureMULTIBIND;
import lombok.Getter;


public class GenericStageWrappedWidget extends Table {

    @Getter
    private final Stage stage;

    private final OrthographicCamera camera;
    protected Matrix4 emptyTransform = new Matrix4();
    private Matrix4 prevTransform = new Matrix4();
    private Matrix4 prevProjection = new Matrix4();

    public GenericStageWrappedWidget(Actor actor) {
        camera = new OrthographicCamera();
        stage = new Stage(new ScreenViewport(camera), new PolygonSpriteBatchMultiTextureMULTIBIND());
        camera.update();

        stage.addActor(actor);
        stage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (actor.hit(x, y, true) == null) {
                    stage.unfocus(actor);
                }
                return super.touchDown(event, x, y, pointer, button);
            }
        });
    }

    @Override
    public void act (float delta) {
        super.act(delta);

        Vector2 temp = new Vector2();
        temp.set(getX(), getY());
        localToScreenCoordinates(temp);
        float x1 = temp.x;
        float y1 = Gdx.graphics.getHeight() - temp.y;

        temp.set(getX() + getWidth(), getY() + getHeight());
        localToScreenCoordinates(temp);
        float x2 = temp.x;
        float y2 = Gdx.graphics.getHeight() - temp.y;

        int screenWidth = (int)(x2 - x1);
        int screenHeight = (int)(y2 - y1);
        stage.getViewport().update(screenWidth, screenHeight, true);
        stage.getViewport().setScreenBounds((int)x1, (int)y1, screenWidth, screenHeight);
        camera.update();
        stage.act();
        HdpiUtils.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Vector2 temp = Pools.obtain(Vector2.class);
        batch.end();

        localToScreenCoordinates(temp.set(0, 0));
        int x = (int)temp.x;
        int y = (int)temp.y;

        localToScreenCoordinates(temp.set(getWidth(), getHeight()));

        int x2 = (int)temp.x;
        int y2 = (int)temp.y;

        int ssWidth = x2 - x;
        int ssHeight = y - y2;

        HdpiUtils.glViewport(x, Gdx.graphics.getHeight() - y, ssWidth, ssHeight);

        prevTransform.set(batch.getTransformMatrix());
        prevProjection.set(batch.getProjectionMatrix());

        batch.setProjectionMatrix(camera.combined);
        batch.setTransformMatrix(emptyTransform);

        batch.begin();

        if (batch instanceof PolygonBatch) {
            Array<Rectangle> stack = new Array<>();
            while (ScissorStack.peekScissors() != null) {
                stack.add(ScissorStack.popScissors());
            }

            batch.end();
            stage.draw();
            batch.begin();

            int idx = stack.size - 1;
            while (idx >= 0) {
                ScissorStack.pushScissors(stack.get(idx));
                idx--;
            }
        }

        batch.end();

        HdpiUtils.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.setProjectionMatrix(prevProjection);
        batch.setTransformMatrix(prevTransform);
        batch.begin();

        Pools.free(temp);

        super.draw(batch, parentAlpha);
    }
}
