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

package com.talosvfx.talos.editor.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.talosvfx.talos.editor.render.Render;

import static com.talosvfx.talos.runtime.vfx.modules.OffsetModule.*;

public class ShapeWidget extends Actor {

    Skin skin;
    Color tmpColor;
    Vector2 tmp = new Vector2();
    ShapeRenderer shapeRenderer;

    Vector2 shapePos = new Vector2();
    Vector2 shapeSize = new Vector2();

    private final int LEFT = 0;
    private final int RIGHT = 1;
    private final int TOP = 2;
    private final int BOTTOM = 3;

    private int selectedSide = -1;
    private boolean centerHover = false;
    private boolean draggingCenter = false;

    private int shapeType = TYPE_SQUARE;

    ChangeListener changeListener;

    public ShapeWidget(Skin skin) {
        this.skin = skin;
        tmpColor = new Color();

        shapeRenderer = Render.instance().shapeRenderer();

        shapePos.set(0, 0);
        shapeSize.set(30, 30);

        addListener(new ClickListener() {

            Vector2 prevPos = new Vector2();
            Vector2 tmp = new Vector2();

            private int hitSide(float x, float y) {
                selectedSide = -1;
                float tolerance = 5f;
                // left side
                float line = getWidth()/2f + shapePos.x - shapeSize.x/2f;
                if(Math.abs(x - line) < tolerance) {
                    selectedSide = LEFT;
                }
                // right side
                line = getWidth()/2f + shapePos.x + shapeSize.x/2f;
                if(Math.abs(x - line) < tolerance) {
                    selectedSide = RIGHT;
                }
                // top side
                line = getHeight()/2f + shapePos.y + shapeSize.y/2f;
                if(Math.abs(y - line) < tolerance) {
                    selectedSide = TOP;
                }
                // bottom side
                line = getHeight()/2f + shapePos.y - shapeSize.y/2f;
                if(Math.abs(y - line) < tolerance) {
                    selectedSide = BOTTOM;
                }

                return selectedSide;
            }

            private boolean hitCenter(float x, float y) {
                tmp.set(shapePos);
                if(tmp.dst(x - getWidth()/2f, y - getHeight()/2f) < 5) {
                    return true;
                }

                return false;
            }


            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                hitSide(x, y);

                centerHover = hitCenter(x, y);

                return super.mouseMoved(event, x, y);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                prevPos.set(x, y);
                hitSide(x, y);
                draggingCenter = hitCenter(x, y);
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);
                if(selectedSide == TOP) {
                    float diff = y - prevPos.y;
                    shapeSize.add(0, diff * 2f);
                    //if(shapeSize.y < 0) shapeSize.y = 0;
                    if(shapeSize.y > getHeight()) shapeSize.y = getHeight();
                    if(y > getHeight()) shapeSize.y = getHeight();
                    if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) shapeSize.x = shapeSize.y;
                    fitSize();
                }
                if(selectedSide == BOTTOM) {
                    float diff = prevPos.y - y;
                    shapeSize.add(0, diff * 2f);
                    //if(shapeSize.y < 0) shapeSize.y = 0;
                    if(shapeSize.y > getHeight()) shapeSize.y = getHeight();
                    if(y > getHeight()) shapeSize.y = getHeight();
                    if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) shapeSize.x = shapeSize.y;
                    fitSize();
                }
                if(selectedSide == LEFT) {
                    float diff = prevPos.x - x;
                    shapeSize.add(diff * 2f, 0f);
                    //if(shapeSize.x < 0) shapeSize.x = 0;
                    if(shapeSize.x > getWidth()) shapeSize.x = getWidth();
                    if(x > getWidth()) shapeSize.x = getWidth();
                    if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) shapeSize.y = shapeSize.x;
                    fitSize();
                }
                if(selectedSide == RIGHT) {
                    float diff = x - prevPos.x;
                    shapeSize.add(diff * 2f, 0f);
                    //if(shapeSize.x < 0) shapeSize.x = 0;
                    if(shapeSize.x > getWidth()) shapeSize.x = getWidth();
                    if(x > getWidth()) shapeSize.x = getWidth();
                    if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) shapeSize.y = shapeSize.x;
                    fitSize();
                }


                if(draggingCenter && selectedSide == -1) {
                    tmp.set(x, y);
                    tmp.sub(prevPos);
                    shapePos.add(tmp);

                    if(shapePos.x > getWidth()/2f) shapePos.x = getWidth()/2f;
                    if(shapePos.y > getHeight()/2f) shapePos.y = getHeight()/2f;
                    if(shapePos.x < -getWidth()/2f) shapePos.x = -getWidth()/2f;
                    if(shapePos.y < -getHeight()/2f) shapePos.y = -getHeight()/2f;

                    //fitting inside parent
                    fitSize();


                }

                if(changeListener != null) changeListener.changed(new ChangeListener.ChangeEvent(), ShapeWidget.this);

                prevPos.set(x, y);
            }

            private void fitSize() {
                if(shapePos.x + shapeSize.x/2f > getWidth()/2f) {
                    shapeSize.x = (getWidth()/2f - shapePos.x) * 2f;
                    //if( shapeSize.x < 0)  shapeSize.x = 0;
                }
                if(shapePos.x - shapeSize.x/2f < -getWidth()/2f) {
                    shapeSize.x = (getWidth()/2f + shapePos.x) * 2f;
                }
                if(shapePos.y + shapeSize.y/2f > getHeight()/2f) {
                    shapeSize.y = (getHeight()/2f - shapePos.y) * 2f;
                    //if( shapeSize.y < 0)  shapeSize.y = 0;
                }
                if(shapePos.y - shapeSize.y/2f < -getHeight()/2f) {
                    shapeSize.y = (getHeight()/2f + shapePos.y) * 2f;
                }
                //if( shapeSize.x < 0)  shapeSize.x = 0;
                //if( shapeSize.y < 0)  shapeSize.y = 0;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                selectedSide = -1;
                centerHover = false;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                draggingCenter = false;
                if(changeListener != null) changeListener.changed(new ChangeListener.ChangeEvent(), ShapeWidget.this);
            }
        });
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {
        tmp.set(0, 0);
        localToStageCoordinates(tmp);

        drawBg(batch, parentAlpha);

        batch.end();
        shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        drawGrid(batch, parentAlpha);
        drawShape(batch, parentAlpha);
        drawTools(batch, parentAlpha);

        shapeRenderer.end();
        batch.begin();

    }

    private void drawTools(Batch batch, float parentAlpha) {
        shapeRenderer.setColor(1f, 1, 0, 0.3f);
        shapeRenderer.rect(tmp.x + getWidth()/2f + shapePos.x - shapeSize.x/2f, tmp.y + getHeight()/2f + shapePos.y - shapeSize.y/2f, shapeSize.x, shapeSize.y);

        if(centerHover) {
            shapeRenderer.setColor(1f, 1, 0, 1f);
        } else {
            shapeRenderer.setColor(1f, 1, 1, 1f);
        }
        shapeRenderer.circle(tmp.x + getWidth()/2f + shapePos.x, tmp.y + getHeight()/2f + shapePos.y, 4f);


        shapeRenderer.setColor(207/255f, 86/255f, 62/255f, 1f);
        if(selectedSide == LEFT) {
            shapeRenderer.rectLine(tmp.x + getWidth()/2f + shapePos.x - shapeSize.x/2f, tmp.y + getHeight()/2f + shapePos.y - shapeSize.y/2f, tmp.x + getWidth()/2f + shapePos.x - shapeSize.x/2f, tmp.y + getHeight()/2f + shapePos.y + shapeSize.y/2f, 1f);
        }
        if(selectedSide == RIGHT) {
            shapeRenderer.rectLine(tmp.x + getWidth()/2f + shapePos.x + shapeSize.x/2f, tmp.y + getHeight()/2f + shapePos.y - shapeSize.y/2f, tmp.x + getWidth()/2f + shapePos.x + shapeSize.x/2f, tmp.y + getHeight()/2f + shapePos.y + shapeSize.y/2f, 1f);
        }
        if(selectedSide == TOP) {
            shapeRenderer.rectLine(tmp.x + getWidth()/2f + shapePos.x - shapeSize.x/2f, tmp.y + getHeight()/2f + shapePos.y + shapeSize.y/2f, tmp.x + getWidth()/2f + shapePos.x + shapeSize.x/2f, tmp.y + getHeight()/2f + shapePos.y + shapeSize.y/2f, 1f);
        }
        if(selectedSide == BOTTOM) {
            shapeRenderer.rectLine(tmp.x + getWidth()/2f + shapePos.x - shapeSize.x/2f, tmp.y + getHeight()/2f + shapePos.y - shapeSize.y/2f, tmp.x + getWidth()/2f + shapePos.x + shapeSize.x/2f, tmp.y + getHeight()/2f + shapePos.y - shapeSize.y/2f, 1f);
        }
    }

    private void drawShape(Batch batch, float parentAlpha) {
        shapeRenderer.setColor(1f, 0, 0, 1f);

        if(shapeType == TYPE_SQUARE) {
            shapeRenderer.rect(tmp.x + getWidth()/2f + shapePos.x - shapeSize.x/2f, tmp.y + getHeight()/2f + shapePos.y - shapeSize.y/2f, shapeSize.x, shapeSize.y);
        } else if(shapeType == TYPE_ELLIPSE) {
            shapeRenderer.ellipse(tmp.x + getWidth()/2f + shapePos.x - shapeSize.x/2f, tmp.y + getHeight()/2f + shapePos.y - shapeSize.y/2f, shapeSize.x, shapeSize.y);
        } else if(shapeType == TYPE_LINE) {
            shapeRenderer.line(tmp.x + getWidth()/2f + shapePos.x - shapeSize.x/2f, tmp.y + getHeight()/2f + shapePos.y - shapeSize.y/2f,
                                   tmp.x + getWidth()/2f + shapePos.x + shapeSize.x/2f, tmp.y + getHeight()/2f + shapePos.y + shapeSize.y/2f);
        }
    }

    private void drawGrid(Batch batch, float parentAlpha) {
        shapeRenderer.setColor(1f, 1f, 1f, 0.2f);
        shapeRenderer.rectLine(tmp.x + getWidth()/2f, tmp.y, tmp.x + getWidth()/2f, tmp.y + getHeight(), 1f);
        shapeRenderer.rectLine(tmp.x, tmp.y + getHeight()/2f, tmp.x + getWidth(), tmp.y + getHeight()/2f, 1f);
    }

    private void drawBg(Batch batch, float parentAlpha) {
        tmpColor.set(0.05f, 0.05f, 0.05f, 1f);
        batch.setColor(tmpColor);
        skin.getDrawable("white").draw(batch, getX(), getY(), getWidth(), getWidth());

        tmpColor.set(0.2f, 0.2f, 0.2f, 1f);
        batch.setColor(tmpColor);
        skin.getDrawable("white").draw(batch, getX()+1, getY()+1, getWidth()-2, getWidth()-2);
    }

    public void setType(int type) {
        shapeType = type;
    }

    public float getPosX() {
        return shapePos.x/getWidth();
    }

    public float getPosY() {
        return shapePos.y/getHeight();
    }

    public float getShapeWidth() {
        return shapeSize.x/getWidth();
    }

    public float getShapeHeight() {
        return shapeSize.y/getHeight();
    }

    public void setListener(ChangeListener listener) {
        this.changeListener = listener;
    }

    public void setPos(float x, float y) {
        shapePos.set(x * getWidth(), y * getHeight());
    }

    public void setShapeSize(float x, float y) {
        shapeSize.set(x * getWidth(), y * getHeight());
    }

    public void resize(float resize) {
        shapeSize.scl(resize);
        shapePos.scl(resize);
    }
}
