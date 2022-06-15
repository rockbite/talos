package com.talosvfx.talos.editor.addons.scene.apps.spriteeditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;

public class EditPanel extends Table {
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
    private Rectangle bounds;

    private Color borderColor;
    private Color sliceLineColor;
    private Color activeSliceLineColor;


    private float leftOffset = 0;
    private float rightOffset = 0;
    private float topOffset = 0;
    private float bottomOffset = 0;
    private SpriteMetadata metadata;

    private int activeSide = 0b0;

    private EditPanelListener editPanelListener;

    // Shapes
    private Image circle;
    private TextureRegion line;

    private Image image;
    private Texture texture;

    public EditPanel(EditPanelListener editPanelListener) {
        circle = new Image(TalosMain.Instance().getSkin().getDrawable("vfx-green"));
        line = TalosMain.Instance().getSkin().getRegion("white");

        this.editPanelListener = editPanelListener;
        this.bounds = new Rectangle();
        borderColor = new Color(0.0f, 0.0f, 1.0f, 1.0f);
        sliceLineColor = new Color(0.0f, 1.0f, 0.0f, 1.0f);
        activeSliceLineColor = new Color(1.0f, 0.0f, 0.0f, 1.0f);

        clip();
        setTouchable(Touchable.enabled);
        addCaptureListener(new InputListener() {
            private boolean isDragging = false;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                last.set(x, y);
                return true;
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                zoom += amountY;
                zoom = MathUtils.clamp(zoom, 0.1f, 100f);
                image.setScale(zoom);
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
                isDragging = true;
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

                image.setPosition(offset.x, offset.y);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                if (editPanelListener != null && isDragging) {
                    editPanelListener.dragStop(getLeft(), getTop(), getTop(), getBottom());
                }
                isDragging = false;
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

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // draw border
        borderColor.a = parentAlpha;
        drawRect(batch, bounds.x, bounds.y, bounds.width * zoom, bounds.height * zoom, borderColor);

        // draw pivot
        borderColor.a = parentAlpha;
        drawCircle(
            batch,
            bounds.x + bounds.width / 2f * zoom,
            bounds.y + bounds.height / 2f * zoom,
            5f,
            borderColor
        );

        // draw segment lines
        sliceLineColor.a = parentAlpha;
        activeSliceLineColor.a = parentAlpha;

        drawSlice(batch, LEFT);
        drawSlice(batch, RIGHT);
        drawSlice(batch, TOP);
        drawSlice(batch, BOTTOM);

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        bounds.x = image.getX() - image.getOriginX() * zoom + texture.getWidth() / 2f;
        bounds.y = image.getY() - image.getOriginY() * zoom + texture.getHeight() / 2f;
        bounds.width = texture.getWidth();
        bounds.height = texture.getHeight();

        image.setOrigin(Align.center);
        image.setScale(zoom);
        image.setPosition(offset.x, offset.y);
    }

    public void show(SpriteMetadata metadata, Texture texture) {
        this.metadata = metadata;
        this.texture = texture;
        this.image = new Image(texture);

        float zoomX = getWidth() / texture.getWidth();
        float zoomY = getHeight() / texture.getHeight();
        zoom = Math.min(zoomX, zoomY);
        float longestSide = Math.max(texture.getWidth(), texture.getHeight());
        offset.set(
            longestSide / 2f * zoom - texture.getWidth() / 2f,
            longestSide / 2f * zoom - texture.getHeight() / 2f
        );
        delta.setZero();
        last.setZero();
        current.setZero();
        leftOffset = 0;
        rightOffset = 0;
        topOffset = 0;
        bottomOffset = 0;
        clearChildren();
        addActor(image);
    }

    private void drawSlice(Batch batch, int side) {
        Color color;
        if (isSame(activeSide, side) && Gdx.input.isTouched()) {
            color = activeSliceLineColor;
        } else {
            color = sliceLineColor;
        }

        float x1, y1, x2, y2;

        float left = metadata.borderData[0] + leftOffset;
        float right = metadata.borderData[1] + rightOffset;
        float top = metadata.borderData[2] + topOffset;
        float bottom = metadata.borderData[3] + bottomOffset;

        switch (side) {
            case LEFT:
                x1 = bounds.x + left * zoom;
                y1 = bounds.y;
                x2 = bounds.x + left * zoom;
                y2 = bounds.y + bounds.height * zoom;
                break;
            case RIGHT:
                x1 = bounds.x + (bounds.width - right) * zoom;
                y1 = bounds.y;
                x2 = bounds.x + (bounds.width - right) * zoom;
                y2 = bounds.y + bounds.height * zoom;
                break;
            case TOP:
                x1 = bounds.x;
                y1 = bounds.y + (bounds.height - top) * zoom;
                x2 = bounds.x + bounds.width * zoom;
                y2 = bounds.y + (bounds.height - top) * zoom;
                break;
            case BOTTOM:
                x1 = bounds.x;
                y1 = bounds.y + bottom * zoom;
                x2 = bounds.x + bounds.width * zoom;
                y2 = bounds.y + bottom * zoom;
                break;
            default:
                return;
        }

        drawLine(batch, x1, y1, x2, y2, color);
    }

    private void drawLine(Batch batch, float x1, float y1, float x2, float y2, Color color) {
        tmp.set(x2, y2).sub(x1, y1);
        float thickness = 1.5f;
        float length = tmp.len();
        float rotation = tmp.angleDeg();
        tmp.scl(0.5f).add(x1, y1); // center points
        Color prev = batch.getColor();
        batch.setColor(color);
        batch.draw(line, tmp.x - 0.5f * length, tmp.y - 0.5f * thickness, length/2f, thickness/2f, length, thickness, 1f, 1f, rotation);
        batch.setColor(prev);
    }

    private void drawRect(Batch batch, float x, float y, float width, float height, Color color) {
        drawLine(batch, x, y, x, y + height, color);
        drawLine(batch, x, y + height, x + width, y + height, color);
        drawLine(batch, x + width, y + height, x + width, y, color);
        drawLine(batch, x + width, y, x, y, color);
    }

    private void drawCircle (Batch batch, float x, float y, float radius, Color color) {
        circle.setSize(radius * 2, radius * 2);
        circle.setPosition(x - radius, y - radius);
        Color prev = batch.getColor();
        batch.setColor(color);
        circle.draw(batch, color.a);
        batch.setColor(prev);
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
        public abstract void dragStop(float left, float right, float top, float bottom);
    }
}
