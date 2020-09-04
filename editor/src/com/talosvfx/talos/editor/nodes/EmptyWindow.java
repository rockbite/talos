package com.talosvfx.talos.editor.nodes;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public abstract class EmptyWindow extends Table {

    private static final Vector2 tmpPosition = new Vector2();
    private static final Vector2 tmpSize = new Vector2();
    private static final int MOVE = 32;
    boolean isMovable;
    boolean isModal;
    boolean isResizable;
    int resizeBorder;
    protected int edge;
    protected boolean dragging;

    public EmptyWindow() {

    }

    public EmptyWindow(Skin skin) {
        init(skin);
    }

    public void init(Skin skin) {
        this.setSkin(skin);
        this.isMovable = true;
        this.resizeBorder = 8;

        this.setTouchable(Touchable.enabled);
        this.setClip(true);

        this.addCaptureListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                EmptyWindow.this.toFront();
                return false;
            }
        });
        this.addListener(new InputListener() {
            float startX;
            float startY;
            float lastX;
            float lastY;

            private void updateEdge(float x, float y) {
                float border = (float)EmptyWindow.this.resizeBorder / 2.0F;
                float width = EmptyWindow.this.getWidth();
                float height = EmptyWindow.this.getHeight();
                float padLeft = EmptyWindow.this.getPadLeft();
                float padBottom = EmptyWindow.this.getPadBottom();
                float padRight = EmptyWindow.this.getPadRight();
                float right = width - padRight;
                EmptyWindow.this.edge = 0;
                if (EmptyWindow.this.isResizable && x >= padLeft - border && x <= right + border && y >= padBottom - border) {
                    EmptyWindow var10000;
                    if (x < padLeft + border) {
                        var10000 = EmptyWindow.this;
                        var10000.edge |= 8;
                    }

                    if (x > right - border) {
                        var10000 = EmptyWindow.this;
                        var10000.edge |= 16;
                    }

                    if (y < padBottom + border) {
                        var10000 = EmptyWindow.this;
                        var10000.edge |= 4;
                    }

                    if (EmptyWindow.this.edge != 0) {
                        border += 25.0F;
                    }

                    if (x < padLeft + border) {
                        var10000 = EmptyWindow.this;
                        var10000.edge |= 8;
                    }

                    if (x > right - border) {
                        var10000 = EmptyWindow.this;
                        var10000.edge |= 16;
                    }

                    if (y < padBottom + border) {
                        var10000 = EmptyWindow.this;
                        var10000.edge |= 4;
                    }
                }

                if (EmptyWindow.this.isMovable && EmptyWindow.this.edge == 0 && y <= height && y >= height - getDragPadTop() && x >= padLeft && x <= right) {
                    EmptyWindow.this.edge = 32;
                }

            }

            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == 0) {
                    this.updateEdge(x, y);
                    EmptyWindow.this.dragging = EmptyWindow.this.edge != 0;
                    this.startX = x;
                    this.startY = y;
                    this.lastX = x - EmptyWindow.this.getWidth();
                    this.lastY = y - EmptyWindow.this.getHeight();
                }

                return EmptyWindow.this.edge != 0 || EmptyWindow.this.isModal;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                EmptyWindow.this.dragging = false;
            }

            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if (EmptyWindow.this.dragging) {
                    float width = EmptyWindow.this.getWidth();
                    float height = EmptyWindow.this.getHeight();
                    float windowX = EmptyWindow.this.getX();
                    float windowY = EmptyWindow.this.getY();
                    float minWidth = EmptyWindow.this.getMinWidth();
                    float maxWidth = EmptyWindow.this.getMaxWidth();
                    float minHeight = EmptyWindow.this.getMinHeight();
                    float maxHeight = EmptyWindow.this.getMaxHeight();
                    Stage stage = EmptyWindow.this.getStage();
                    boolean clampPosition = stage != null && EmptyWindow.this.getParent() == stage.getRoot();
                    float amountY;
                    if ((EmptyWindow.this.edge & 32) != 0) {
                        amountY = x - this.startX;
                        float amountYx = y - this.startY;
                        windowX += amountY;
                        windowY += amountYx;
                    }

                    if ((EmptyWindow.this.edge & 8) != 0) {
                        amountY = x - this.startX;
                        if (width - amountY < minWidth) {
                            amountY = -(minWidth - width);
                        }

                        if (clampPosition && windowX + amountY < 0.0F) {
                            amountY = -windowX;
                        }

                        width -= amountY;
                        windowX += amountY;
                    }

                    if ((EmptyWindow.this.edge & 4) != 0) {
                        amountY = y - this.startY;
                        if (height - amountY < minHeight) {
                            amountY = -(minHeight - height);
                        }

                        if (clampPosition && windowY + amountY < 0.0F) {
                            amountY = -windowY;
                        }

                        height -= amountY;
                        windowY += amountY;
                    }

                    if ((EmptyWindow.this.edge & 16) != 0) {
                        amountY = x - this.lastX - width;
                        if (width + amountY < minWidth) {
                            amountY = minWidth - width;
                        }

                        if (clampPosition && windowX + width + amountY > stage.getWidth()) {
                            amountY = stage.getWidth() - windowX - width;
                        }

                        width += amountY;
                    }

                    if ((EmptyWindow.this.edge & 2) != 0) {
                        amountY = y - this.lastY - height;
                        if (height + amountY < minHeight) {
                            amountY = minHeight - height;
                        }

                        if (clampPosition && windowY + height + amountY > stage.getHeight()) {
                            amountY = stage.getHeight() - windowY - height;
                        }

                        height += amountY;
                    }

                    EmptyWindow.this.setBounds((float)Math.round(windowX), (float)Math.round(windowY), (float)Math.round(width), (float)Math.round(height));
                }
            }

            public boolean mouseMoved(InputEvent event, float x, float y) {
                this.updateEdge(x, y);
                return EmptyWindow.this.isModal;
            }

            public boolean scrolled(InputEvent event, float x, float y, int amount) {
                return EmptyWindow.this.isModal;
            }

            public boolean keyDown(InputEvent event, int keycode) {
                return EmptyWindow.this.isModal;
            }

            public boolean keyUp(InputEvent event, int keycode) {
                return EmptyWindow.this.isModal;
            }

            public boolean keyTyped(InputEvent event, char character) {
                return EmptyWindow.this.isModal;
            }
        });
    }

    public void draw(Batch batch, float parentAlpha) {
        Stage stage = this.getStage();
        if (stage != null && stage.getKeyboardFocus() == null) {
            stage.setKeyboardFocus(this);
        }

        super.draw(batch, parentAlpha);
    }

    public Actor hit(float x, float y, boolean touchable) {
        if (!this.isVisible()) {
            return null;
        } else {
            Actor hit = super.hit(x, y, touchable);
            if (hit != null || !this.isModal || touchable && this.getTouchable() != Touchable.enabled) {
                float height = this.getHeight();
                if (hit != null && hit != this) {
                    if (y <= height && y >= height - this.getPadTop() && x >= 0.0F && x <= this.getWidth()) {
                        Object current;
                        for(current = hit; ((Actor)current).getParent() != this; current = ((Actor)current).getParent()) {
                        }

                        if (this.getCell((Actor)current) != null) {
                            return this;
                        }
                    }

                    return hit;
                } else {
                    return hit;
                }
            } else {
                return this;
            }
        }
    }

    public boolean isMovable() {
        return this.isMovable;
    }

    public void setMovable(boolean isMovable) {
        this.isMovable = isMovable;
    }

    public boolean isModal() {
        return this.isModal;
    }

    public void setModal(boolean isModal) {
        this.isModal = isModal;
    }

    public boolean isResizable() {
        return this.isResizable;
    }

    public void setResizable(boolean isResizable) {
        this.isResizable = isResizable;
    }

    public void setResizeBorder(int resizeBorder) {
        this.resizeBorder = resizeBorder;
    }

    public boolean isDragging() {
        return this.dragging;
    }

    public float getPrefWidth() {
        return Math.max(super.getPrefWidth(), getTitlePrefWidth() + this.getPadLeft() + this.getPadRight());
    }

    public abstract float getTitlePrefWidth();

    public abstract float getDragPadTop();
}
