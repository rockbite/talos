package com.talosvfx.talos.editor.addons.scene.apps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;

import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line;

public class EditPanel extends Container<Image> {
    private static final int LEFT = 0b1;
    private static final int RIGHT = 0b10;
    private static final int TOP = 0b100;
    private static final int BOTTOM = 0b1000;

    private float zoom = 1.0f;
    private Vector2 offset = new Vector2();
    private Vector2 current = new Vector2();
    private Vector2 last = new Vector2();
    private Vector2 delta = new Vector2();
    private Vector2 tmp = new Vector2();
    private Color borderColor;
    private Color sliceLineColor;
    private Color activeSliceLineColor;
    private Rectangle bounds;
    private float leftOffset = 0;
    private float rightOffset = 0;
    private float topOffset = 0;
    private float bottomOffset = 0;
    private SpriteMetadata metadata;
    private int activeSide = 0b0;

    private ShapeRenderer shapeRenderer;

    public EditPanel (ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
        this.bounds = new Rectangle();
        borderColor = new Color(0.0f, 0.0f, 1.0f, 1.0f);
        sliceLineColor = new Color(0.0f, 1.0f, 0.0f, 1.0f);
        activeSliceLineColor = new Color(1.0f, 0.0f, 0.0f, 1.0f);

        clip();
        setTouchable(Touchable.enabled);
        addCaptureListener(new DragListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                last.set(x, y);
                return true;
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                zoom += amountY;
                zoom = MathUtils.clamp(zoom, 0.1f, 100f);
                getActor().setScale(zoom);
                return true;
            }

            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                if (activeSide == 0) { // move image
                    current.set(x, y);
                    delta.set(current.x, current.y);
                    delta.sub(last);
                    offset.add(delta.x, delta.y);
                    last.set(x, y);
                } else { // line is selected, move it instead
                    tmp.set(Gdx.input.getX(), Gdx.input.getY());
                    screenToLocalCoordinates(tmp);
                    if (isLeft(activeSide)) {
                        float left = metadata.borderData[0] + leftOffset;
                        float delta = tmp.x - (bounds.x + left * zoom);
                        delta /= zoom;
                        leftOffset += delta;
                    }
                }

                getActor().setPosition(offset.x, offset.y);
            }

            @Override
            public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                TalosMain.Instance().UIStage().getStage().setScrollFocus(EditPanel.this);
            }

            @Override
            public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                if (pointer != -1) return; // Only care about exit/enter from mouse move
                TalosMain.Instance().UIStage().getStage().setScrollFocus(null);
            }
        });
    }

    @Override
    public void drawChildren(Batch batch, float parentAlpha) {
        super.drawChildren(batch, parentAlpha);

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
        shapeRenderer.begin(Line);
        // draw border
        borderColor.a = parentAlpha;
        shapeRenderer.setColor(borderColor);

        shapeRenderer.rect(
                bounds.x, bounds.y,
                bounds.width * zoom, bounds.height * zoom
        );

        // draw pivot
        borderColor.a = parentAlpha;
        shapeRenderer.setColor(borderColor);
        shapeRenderer.circle(bounds.x + bounds.width / 2f * zoom, bounds.y + bounds.height / 2f * zoom,  1 * zoom);

        // draw segment lines
        sliceLineColor.a = parentAlpha;
        activeSliceLineColor.a = parentAlpha;

        float x1, y1, x2, y2;
        float left = metadata.borderData[0] + leftOffset;
        float right = metadata.borderData[1] + rightOffset;
        if (left > bounds.width - right) {
            float tmp = left;
            left = right;
            right = tmp;
        }

        // draw left
        x1 = bounds.x + left * zoom;
        y1 = bounds.y;
        x2 = bounds.x + left * zoom;
        y2 = bounds.y + bounds.height * zoom;
        drawLine(shapeRenderer, x1, y1, x2, y2, LEFT);

        // draw right
        x1 = bounds.x + (bounds.width - right) * zoom;
        y1 = bounds.y;
        x2 = bounds.x + (bounds.width - right) * zoom;
        y2 = bounds.y + bounds.height * zoom;
        drawLine(shapeRenderer, x1, y1, x2, y2, RIGHT);

        float top = metadata.borderData[2] + topOffset;
        float bottom = metadata.borderData[3] + bottomOffset;
        if (bottom > bounds.height - top) {
            float tmp = bottom;
            bottom = top;
            top = tmp;
        }

        // draw top
        x1 = bounds.x;
        y1 = bounds.y + (bounds.height - top) * zoom;
        x2 = bounds.x + bounds.width * zoom;
        y2 = bounds.y + (bounds.height - top) * zoom;
        drawLine(shapeRenderer, x1, y1, x2, y2, TOP);

        // draw bottom
        x1 = bounds.x;
        y1 = bounds.y + bottom * zoom;
        x2 = bounds.x + bounds.width * zoom;
        y2 = bounds.y + bottom * zoom;
        drawLine(shapeRenderer, x1, y1, x2, y2, BOTTOM);

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        TalosMain.Instance().setCursor(null);

        Image patch = getActor();
        bounds.x = getActor().getX() - getActor().getOriginX() * zoom + getActor().getWidth() / 2f;
        bounds.y = getActor().getY() - getActor().getOriginY() * zoom + getActor().getHeight() / 2f;
        bounds.width = getActor().getWidth();
        bounds.height = getActor().getHeight();

        patch.setOrigin(Align.center);
        patch.setScale(zoom);
        patch.setPosition(offset.x, offset.y);

        // set the active side
        activeSide = 0;
        tmp.set(Gdx.input.getX(), Gdx.input.getY());
        screenToLocalCoordinates(tmp);
        float x1, y1, x2, y2;
        float dist = 3;
        float tmpDist;
        float left = metadata.borderData[0] + leftOffset;
        float right = metadata.borderData[1] + rightOffset;
        if (left > bounds.width - right) {
            float tmp = left;
            left = right;
            right = tmp;
        }

        // check left
        x1 = bounds.x + left * zoom;
        y1 = bounds.y;
        x2 = bounds.x + left * zoom;
        y2 = bounds.y + bounds.height * zoom;
        tmpDist = Intersector.distanceLinePoint(x1, y1, x2, y2, tmp.x, tmp.y);
        // scale bounds for check
        bounds.width *= zoom;
        bounds.height *= zoom;
        if (tmpDist <= dist && bounds.contains(tmp)) {
            activeSide = LEFT;
        }
        // bring dimensions back
        bounds.width /= zoom;
        bounds.height /= zoom;

        // check right
        x1 = bounds.x + (bounds.width - right) * zoom;
        y1 = bounds.y;
        x2 = bounds.x + (bounds.width - right) * zoom;
        y2 = bounds.y + bounds.height * zoom;
        tmpDist = Intersector.distanceLinePoint(x1, y1, x2, y2, tmp.x, tmp.y);
        // scale bounds for check
        bounds.width *= zoom;
        bounds.height *= zoom;
        if (tmpDist <= dist && bounds.contains(tmp)) {
            activeSide = RIGHT;
        }
        // bring dimensions back
        bounds.width /= zoom;
        bounds.height /= zoom;

        float top = metadata.borderData[2] + topOffset;
        float bottom = metadata.borderData[3] + bottomOffset;
        if (bottom > bounds.height - top) {
            float tmp = bottom;
            bottom = top;
            top = tmp;
        }

        // check top
        x1 = bounds.x;
        y1 = bounds.y + (bounds.height - top) * zoom;
        x2 = bounds.x + bounds.width * zoom;
        y2 = bounds.y + (bounds.height - top) * zoom;
        tmpDist = Intersector.distanceLinePoint(x1, y1, x2, y2, tmp.x, tmp.y);
        // scale bounds for check
        bounds.width *= zoom;
        bounds.height *= zoom;
        if (tmpDist <= dist && bounds.contains(tmp)) {
            activeSide = TOP;
        }
        // bring dimensions back
        bounds.width /= zoom;
        bounds.height /= zoom;

        // check bottom
        x1 = bounds.x;
        y1 = bounds.y + bottom * zoom;
        x2 = bounds.x + bounds.width * zoom;
        y2 = bounds.y + bottom * zoom;
        tmpDist = Intersector.distanceLinePoint(x1, y1, x2, y2, tmp.x, tmp.y);
        // scale bounds for check
        bounds.width *= zoom;
        bounds.height *= zoom;
        if (tmpDist <= dist && bounds.contains(tmp)) {
            activeSide = BOTTOM;
        }
        // bring dimensions back
        bounds.width /= zoom;
        bounds.height /= zoom;
    }

    public void show(SpriteMetadata metadata, NinePatch patch) {
        this.metadata = metadata;
        Image patchImage = new Image(patch);

        float zoomX = getWidth() / patchImage.getWidth();
        float zoomY = getHeight() / patchImage.getHeight();
        zoom = Math.min(zoomX, zoomY);

        offset.set(patchImage.getWidth() / 2f * zoom - patchImage.getWidth() / 2f, patchImage.getHeight() / 2f * zoom  - patchImage.getHeight() / 2f);
        delta.setZero();
        last.setZero();
        current.setZero();
        setActor(patchImage);
    }

    private void drawLine(ShapeRenderer shapeRenderer, float x1, float y1, float x2, float y2, int side) {
        if (isSame(activeSide, side)) {
            TalosMain.Instance().setCursor(TalosMain.Instance().moveRulerCursor);
        }
        if (isSame(activeSide, side) && Gdx.input.isTouched()) {
            shapeRenderer.setColor(activeSliceLineColor);
        } else {
            shapeRenderer.setColor(sliceLineColor);
        }
        shapeRenderer.line(x1, y1, x2, y2);
    }

    private static boolean isLeft(int side) {
        return (side & LEFT) != 0;
    }

    private static boolean isRight(int side) {
        return (side & RIGHT) != 0;
    }

    private static boolean isTop(int side) {
        return (side & TOP) != 0;
    }

    private static boolean isBottom(int side) {
        return (side & BOTTOM) != 0;
    }

    private static boolean isHorizontal(int side) {
        return isLeft(side) || isRight(side);
    }

    private static boolean isVertical(int side) {
        return isTop(side) || isBottom(side);
    }

    private static boolean isSame(int side1, int side2) {
        return (side1 & side2) != 0;
    }
}
