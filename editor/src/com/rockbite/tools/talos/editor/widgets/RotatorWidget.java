/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.rockbite.tools.talos.editor.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class RotatorWidget extends Actor {

    private Skin skin;

    private Image image;

    private float value;

    private ChangeListener listener;

    private boolean normalize = false;

    private Label label;

    private boolean isActive = false;

    public RotatorWidget(Skin skin) {
        this.skin = skin;
        image = new Image(skin.getDrawable("rotator"));

        label = new Label("", skin, "small");

        addListener(new ClickListener() {

            Vector2 tmp = new Vector2();
            float prevAngle;

            public void applyAngle(float x, float y) {
                float newAngle = tmp.set(x - image.getOriginX(), y - image.getOriginY()).angle();
                if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                    float step = 15f;
                    float offset = newAngle % step;
                    if(offset < step/2f) {
                        newAngle -= offset;
                    } else {
                        newAngle += (step - offset);
                    }

                    if(prevAngle == 360 && newAngle == 0) newAngle = 360;
                    if(prevAngle == 0 && newAngle == 360) newAngle = 0;
                }

                if(prevAngle >= 0 && prevAngle <= 90 && newAngle > 270 && newAngle <= 360) {
                    newAngle = 0;
                }
                if(prevAngle <= 360 && prevAngle >= 270 && newAngle > 0 && newAngle <= 90) {
                    newAngle = 360;
                }

                value = (int)newAngle;

                if(listener != null) {
                    listener.changed(new ChangeListener.ChangeEvent(), RotatorWidget.this);
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                prevAngle = value;
                applyAngle(x, y);
                isActive = true;
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
                isActive = false;
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {

        getSkin().getDrawable("rotator-bg").draw(batch, getX(), getY(), getWidth(), getHeight());

        label.setText((int)value + "");
        label.setPosition(getX() + getWidth()/2f - label.getPrefWidth()/2f, getY() + getHeight()/2f + 20 - label.getPrefHeight()/2f);
        label.draw(batch, parentAlpha);


        image.setDrawable(skin.getDrawable("rotator"));
        if(isActive) {
            image.setDrawable(skin.getDrawable("rotator-active"));
        }
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
