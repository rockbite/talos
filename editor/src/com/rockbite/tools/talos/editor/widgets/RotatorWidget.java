package com.rockbite.tools.talos.editor.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class RotatorWidget extends Actor {

    private Skin skin;

    private Image image;

    private float value;

    private ChangeListener listener;

    private boolean normalize = false;

    public RotatorWidget(Skin skin) {
        this.skin = skin;
        image = new Image(skin.getDrawable("rotator"));

        addListener(new ClickListener() {

            Vector2 tmp = new Vector2();
            float prevAngle;

            public void applyAngle(float x, float y) {
                float newAngle = tmp.set(x - image.getOriginX(), y - image.getOriginY()).angle();
                if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                    float step = 15f;
                    float offfset = newAngle % step;
                    if(offfset < step/2f) {
                        newAngle -= offfset;
                    } else {
                        newAngle += (step - offfset);
                    }
                }

                if(prevAngle > 0 && prevAngle <= 180 && (newAngle == 0 || newAngle == 360)) newAngle = 0;
                if(prevAngle < 360 && prevAngle > 180 && (newAngle == 0 || newAngle == 360)) newAngle = 360;

                value = newAngle;

                if(listener != null) {
                    listener.changed(new ChangeListener.ChangeEvent(), RotatorWidget.this);
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                prevAngle = value;
                applyAngle(x, y);
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                prevAngle = value;
                applyAngle(x, y);
                super.touchDragged(event, x, y, pointer);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {

        image.setPosition(getX(), getY());
        image.setSize(getWidth(), getHeight());
        image.setOrigin(getWidth()/2f, getHeight()/2f);
        image.setRotation(value - 90f);

        image.draw(batch, parentAlpha);
    }

    public Skin getSkin() {
        return skin;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getValue() {
        if(normalize) {
            return value/360f;
        } else {
            return value;
        }
    }

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }
}
