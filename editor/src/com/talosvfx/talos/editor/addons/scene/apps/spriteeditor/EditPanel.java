package com.talosvfx.talos.editor.addons.scene.apps.spriteeditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;

import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line;

public class EditPanel extends Container<Image> {
    public static final int LEFT = 0b1;
    public static final int RIGHT = 0b10;
    public static final int TOP = 0b100;
    public static final int BOTTOM = 0b1000;

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

    private EditPanelListener editPanelListener;

    private ShapeRenderer shapeRenderer;

    public EditPanel(ShapeRenderer shapeRenderer, EditPanelListener editPanelListener) {
        this.shapeRenderer = shapeRenderer;
        this.editPanelListener = editPanelListener;
        this.bounds = new Rectangle();
        borderColor = new Color(0.0f, 0.0f, 1.0f, 1.0f);
        sliceLineColor = new Color(0.0f, 1.0f, 0.0f, 1.0f);
        activeSliceLineColor = new Color(1.0f, 0.0f, 0.0f, 1.0f);

        clip();
        setTouchable(Touchable.enabled);
        addCaptureListener(new InputListener() {

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
            public boolean mouseMoved(InputEvent event, float x, float y) {
                // set the active side
                activeSide = 0;
                tmp.set(x, y);
                float x1, y1, x2, y2;
                float dist = 3;
                float tmpDist;
                float left = metadata.borderData[0] + leftOffset;
                float right = metadata.borderData[1] + rightOffset;

                // check left
                x1 = bounds.x + left * zoom;
                y1 = bounds.y;
                x2 = bounds.x + left * zoom;
                y2 = bounds.y + bounds.height * zoom;
                tmpDist = Intersector.distanceLinePoint(x1, y1, x2, y2, tmp.x, tmp.y);
                // scale bounds for check
                bounds.width *= zoom;
                bounds.height *= zoom;
                if (tmpDist <= dist && bounds.contains(tmp) && !isHorizontal(activeSide)) {
                    activeSide = activeSide | LEFT;
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
                if (tmpDist <= dist && bounds.contains(tmp) && !isHorizontal(activeSide)) {
                    activeSide = activeSide | RIGHT;
                }
                // bring dimensions back
                bounds.width /= zoom;
                bounds.height /= zoom;

                float top = metadata.borderData[2] + topOffset;
                float bottom = metadata.borderData[3] + bottomOffset;

                // check top
                x1 = bounds.x;
                y1 = bounds.y + (bounds.height - top) * zoom;
                x2 = bounds.x + bounds.width * zoom;
                y2 = bounds.y + (bounds.height - top) * zoom;
                tmpDist = Intersector.distanceLinePoint(x1, y1, x2, y2, tmp.x, tmp.y);
                // scale bounds for check
                bounds.width *= zoom;
                bounds.height *= zoom;
                if (tmpDist <= dist && bounds.contains(tmp) && !isVertical(activeSide)) {
                    activeSide = activeSide | TOP;
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
                if (tmpDist <= dist && bounds.contains(tmp) && !isVertical(activeSide)) {
                    activeSide = activeSide | BOTTOM;
                }
                // bring dimensions back
                bounds.width /= zoom;
                bounds.height /= zoom;

                if (isHorizontal(activeSide) && isVertical(activeSide)) {
                    TalosMain.Instance().setCursor(TalosMain.Instance().moveAllDirections);
                } else if (isVertical(activeSide)) {
                    TalosMain.Instance().setCursor(TalosMain.Instance().moveVerticallyCursor);
                } else if (isHorizontal(activeSide)) {
                    TalosMain.Instance().setCursor(TalosMain.Instance().moveHorizontallyCursor);
                } else {
                    TalosMain.Instance().setCursor(null);
                }
                return super.mouseMoved(event, x, y);
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
                    if (isHorizontal(activeSide)) {
                        float left = metadata.borderData[0] + leftOffset;
                        float right = metadata.borderData[1] + rightOffset;
                        if (left > bounds.width - right) { // swap active side
                            activeSide ^= 1 << 0;
                            activeSide ^= 1 << 1;
                        }
                        if (isLeft(activeSide)) {
                            float delta = tmp.x - (bounds.x + left * zoom);
                            delta /= zoom;
                            leftOffset += delta;
                            leftOffset = MathUtils.clamp(leftOffset, -metadata.borderData[0], bounds.width - metadata.borderData[0]);
                        } else {
                            right = bounds.width - (metadata.borderData[1] + rightOffset);
                            float delta = tmp.x - (bounds.x + right * zoom);
                            delta /= zoom;
                            rightOffset -= delta;
                            rightOffset = MathUtils.clamp(rightOffset, -metadata.borderData[1], bounds.width - metadata.borderData[1]);
                        }
                    }
                    if (isVertical(activeSide)) {
                        float top = metadata.borderData[2] + topOffset;
                        float bottom = metadata.borderData[3] + bottomOffset;
                        if (bottom > bounds.height - top) {
                            activeSide ^= 1 << 2;
                            activeSide ^= 1 << 3;
                        }
                        if (isTop(activeSide)) {
                            top = bounds.height - (metadata.borderData[2] + topOffset);
                            float delta = tmp.y - (bounds.y + top * zoom);
                            delta /= zoom;
                            topOffset -= delta;
                            topOffset = MathUtils.clamp(topOffset, -metadata.borderData[2], bounds.height - metadata.borderData[2]);
                        } else {
                            float delta = tmp.y - (bounds.y + bottom * zoom);
                            delta /= zoom;
                            bottomOffset += delta;
                            bottomOffset = MathUtils.clamp(bottomOffset, -metadata.borderData[3], bounds.height - metadata.borderData[3]);
                        }
                    }

                    editPanelListener.changed(getLeft(), getRight(), getTop(), getBottom());
                }

                getActor().setPosition(offset.x, offset.y);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                editPanelListener.dragStop(getLeft(), getRight(), getTop(), getBottom());
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

        Image patch = getActor();
        bounds.x = getActor().getX() - getActor().getOriginX() * zoom + getActor().getWidth() / 2f;
        bounds.y = getActor().getY() - getActor().getOriginY() * zoom + getActor().getHeight() / 2f;
        bounds.width = getActor().getWidth();
        bounds.height = getActor().getHeight();

        patch.setOrigin(Align.center);
        patch.setScale(zoom);
        patch.setPosition(offset.x, offset.y);
    }

    public void show(SpriteMetadata metadata, Texture patch) {
        this.metadata = metadata;
        Image patchImage = new Image(patch);

        float zoomX = getWidth() / patchImage.getWidth();
        float zoomY = getHeight() / patchImage.getHeight();
        zoom = Math.min(zoomX, zoomY);

        offset.set(patchImage.getWidth() / 2f * zoom - patchImage.getWidth() / 2f, patchImage.getHeight() / 2f * zoom  - patchImage.getHeight() / 2f);
        delta.setZero();
        last.setZero();
        current.setZero();
        leftOffset = 0;
        rightOffset = 0;
        topOffset = 0;
        bottomOffset = 0;
        setActor(patchImage);
    }

    private void drawLine(ShapeRenderer shapeRenderer, float x1, float y1, float x2, float y2, int side) {
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

    public float getLeft () {
        return metadata.borderData[0] + leftOffset;
    }

    public float getRight () {
        return metadata.borderData[1] + rightOffset;
    }

    public float getTop () {
        return metadata.borderData[2] + topOffset;
    }

    public float getBottom () {
        return metadata.borderData[3] + bottomOffset;
    }

    public void set(int side, float value) {
        if (isLeft(side)) {
            leftOffset = value - metadata.borderData[0];
        } else if(isRight(side)) {
            rightOffset = value - metadata.borderData[1];
        } else if(isTop(side)) {
            topOffset = value - metadata.borderData[2];
        } else if(isBottom(side)) {
            bottomOffset = value - metadata.borderData[3];
        }
    }

    public static abstract class EditPanelListener {
        public abstract void changed(float left, float right, float top, float bottom);

        public abstract void dragStop(float left, float righ, float top, float bottom);
    }
}
