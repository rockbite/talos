package com.rockbite.tools.talos.editor.widgets;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.rockbite.tools.talos.editor.tools.ColorPoint;
import com.rockbite.tools.talos.editor.widgets.ui.GradientImage;
import com.rockbite.tools.talos.runtime.modules.GradientColorModule;

public class GradientWidget extends Actor {

    private Vector2 tmp = new Vector2();

    private Vector2 areaPos = new Vector2();
    private Vector2 areaSize = new Vector2();

    private GradientColorModule module;

    private Skin skin;

    private GradientWidgetListener listener;

    private GradientImage gradientImage;

    private Color tmpColor = new Color();

    public interface GradientWidgetListener {
        public void colorPickerShow(ColorPoint point);
    }

    public GradientWidget(Skin skin) {
        this.skin = skin;

        gradientImage = new GradientImage(skin);

        addListener(new ClickListener() {

            private int draggingPoint = -1;

            private long clickTime;

            private boolean justRemoved = false;

            public int hit(float x, float y) {
                Array<ColorPoint> points = module.getPoints();

                for(int i = 0; i < points.size; i++) {
                    ColorPoint colorPoint = points.get(i);
                    float pos = areaPos.x + areaSize.x * colorPoint.pos;

                    if(Math.abs(x - pos) < 10) {
                        return i;
                    }
                }

                return -1;
            }

            public void setPosToMouse(float x, float y) {
                Array<ColorPoint> points = module.getPoints();

                float pos = (x - areaPos.x)/areaSize.x;

                float leftBound = 0f;
                float rightBound = 1f;

                if(points.size - 1 >= draggingPoint + 1) {
                    rightBound = points.get(draggingPoint+1).pos;
                }
                if(draggingPoint > 0) {
                    leftBound = points.get(draggingPoint-1).pos;
                }

                if(pos < leftBound) {
                    pos = leftBound;
                }
                if(pos > rightBound) {
                    pos = rightBound;
                }

                points.get(draggingPoint).pos = pos;
                updateGradientData();
            }


            private void doubleClick(float x, float y) {
                int hitIndex = hit(x, y);
                if(hitIndex >= 0) {
                    if(module.getPoints().size > 1) {
                        module.removePoint(hitIndex);
                        updateGradientData();
                        justRemoved = true;
                    }
                }
            }

            private void rightClick(InputEvent event, float x, float y) {
                int hit = hit(x, y);
                if(hit >= 0) {
                    if(listener != null) {
                        listener.colorPickerShow(module.getPoints().get(hit));
                    }
                    event.handle();
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Array<ColorPoint> points = module.getPoints();

                if(button == Input.Buttons.RIGHT) {
                    return true;
                }

                long now = TimeUtils.millis();

                justRemoved = false;

                if(now - clickTime < 200 && button == 0) {
                    // this is a doubleClick
                    doubleClick(x, y);
                }

                clickTime = now;

                draggingPoint = hit(x, y);

                if(!justRemoved) {
                    if (draggingPoint == -1) {
                        float pos = (x - areaPos.x) / areaSize.x;
                        ColorPoint point = module.createPoint(gePosColor(pos), pos);
                        draggingPoint = points.indexOf(point, true);
                    } else {
                        setPosToMouse(x, y);
                    }
                }

                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);


                if(draggingPoint >= 0) {
                    setPosToMouse(x, y);
                }
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);

                if(button == Input.Buttons.RIGHT) {
                    rightClick(event, x, y);

                    return;
                }

                draggingPoint = -1;
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {

            }
        });
    }

    public Color gePosColor(float pos) {
        tmpColor.set(module.getPosColor(pos));

        return tmpColor;
    }

    public void setListener(GradientWidgetListener listener) {
        this.listener = listener;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        areaPos.set(11, 15);
        areaSize.set(getWidth() - 20, getHeight()-10);
    }

    public Skin getSkin() {
        return skin;
    }

    public void setModule(GradientColorModule module) {
        this.module = module;
        updateGradientData();
    }

    public void updateGradientData() {
        gradientImage.setPoints(module.getPoints());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {

        gradientImage.setPosition(getX() + areaPos.x, getY() + areaPos.y);
        gradientImage.setSize(areaSize.x, areaSize.y);
        gradientImage.draw(batch, parentAlpha);

        drawPoints(batch, parentAlpha);
    }

    public void drawPoints(Batch batch, float parentAlpha) {

        Array<ColorPoint> points = module.getPoints();

        for(int i = 0; i < points.size; i++) {
            Drawable backgroundFrame = getSkin().getDrawable("triangle");
            batch.setColor(points.get(i).color);
            backgroundFrame.draw(batch, getX() + areaPos.x + areaSize.x * points.get(i).pos - 9, getY() + areaPos.y - 16f, 18f, 16f);
        }

    }
}
