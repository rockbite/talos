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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Comparator;

public class CurveWidget extends Actor implements CurveDataProvider {

    private Color bgColor = new Color(0.82f, 0.82f, 0.82f, 1f);
    private Color lineColor = new Color(154/255f, 23/255f, 48/255f, 0.7f);
    private Color pointColor = new Color(175/255f, 42/255f, 67/255f, 1f);
    private Color gridColor = new Color(0.5f, 0.5f, 0.5f, 0.4f);
    private Vector2 tmp = new Vector2();

    private ShapeRenderer shapeRenderer = new ShapeRenderer();

    float width;
    float height;

    float pointSize = 7f;

    private CurveDataProvider curveDataProvider;
    private Array<Vector2> points = new Array<>();
    Comparator<Vector2> comparator = new Comparator<Vector2>() {
        @Override
        public int compare(Vector2 o1, Vector2 o2) {
            if(o1.x < o2.x) return -1;
            if(o1.x > o2.x) return 1;

            return 0;
        }
    };

    private Skin skin;

    public CurveWidget(Skin skin) {

        curveDataProvider = this;
        resetPoints();

        this.skin = skin;

        addListener(new ClickListener() {

            Vector2 vec1 = new Vector2();
            Vector2 vec2 = new Vector2();

            Vector2 prev = new Vector2();

            private int draggingPoint = -1;

            private long clickTime;

            private boolean justRemoved = false;
            private boolean justCreated = false;

            private void norm(Vector2 vec) {
                vec.sub(3f, 2f);
                vec.scl(1f/width, 1f/height);
            }

            /**
             * both positions normalized
             */
            private boolean hit(Vector2 point, Vector2 mouse) {
                vec2.set((pointSize / width) * 2f, (pointSize / height) * 2f); // vec2 is now hit box (hit box is twice the size)

                if(mouse.x >= point.x - vec2.x/2f &&
                        mouse.x <= point.x + vec2.x/2f &&
                        mouse.y >= point.y - vec2.y/2f &&
                        mouse.y <= point.y + vec2.y/2f) {
                    return true;
                }

                return false;
            }

            private void doubleClick(float x, float y) {
                vec1.set(x, y);
                norm(vec1);

                // we need to create a new point if the space is empty
                Array<Vector2> points = curveDataProvider.getPoints();

                for(int i = 0; i < points.size; i++) {
                    Vector2 point = points.get(i);
                    if(hit(point, vec1) && !justCreated) {
                        // then let's delete this point
                        curveDataProvider.removePoint(i);
                        justRemoved = true;
                        break;
                    }
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                vec1.set(x, y);
                norm(vec1);

                long now = TimeUtils.millis();

                justRemoved = false;

                if(now - clickTime < 200 && button == 0) {
                    // this is a doubleClick
                    doubleClick(x, y);
                }

                clickTime = now;

                prev.set(vec1);

                draggingPoint = -1;

                if(button == 1) return true;

                Array<Vector2> points = curveDataProvider.getPoints();

                boolean wasHit = false;

                for(int i = 0; i < points.size; i++) {
                    Vector2 point = points.get(i);
                    if(hit(point, vec1)) {
                        // yo it's a hit we are now dragging this point
                        wasHit = true;
                        draggingPoint = i;
                        break;
                    }
                }

                justCreated = false;
                if(!wasHit && !justRemoved) {
                    // great we can create new point here
                    draggingPoint = curveDataProvider.createPoint(vec1.x, vec1.y);
                    justCreated = true;
                }

                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);

                vec1.set(x, y);
                norm(vec1);

                if(draggingPoint>= 0) {
                    Vector2 point = curveDataProvider.getPoints().get(draggingPoint);
                    // we are dragging a point
                    point.set(vec1);

                    float leftBound = getLeftBound(draggingPoint);
                    float rightBound = getRightBound(draggingPoint);

                    if(point.x < leftBound) point.x = leftBound;
                    if(point.x > rightBound) point.x = rightBound;
                    if(point.y < 0) point.y = 0;
                    if(point.y > 1) point.y = 1;
                }

                prev.set(vec1);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);

                draggingPoint = -1;
            }

            private float getLeftBound(int index) {
                if(index == 0) return 0;
                return curveDataProvider.getPoints().get(index-1).x;
            }

            private float getRightBound(int index) {
                if(index == curveDataProvider.getPoints().size - 1) return 1;
                return curveDataProvider.getPoints().get(index+1).x;
            }

        });
    }

    private void resetPoints() {
        // need to guarantee at least one point
        points = new Array<>();
        Vector2 point = new Vector2(0, 0.5f);
        points.add(point);
    }

    public void setDataProvider(CurveDataProvider dataProvider) {
        this.curveDataProvider = dataProvider;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        width = getWidth() - 4f;
        height = getHeight() - 4f;

        Drawable backgroundFrame = getSkin().getDrawable("white");
        batch.setColor(Color.BLACK);
        backgroundFrame.draw(batch, getX(), getY(), getWidth(), getHeight());

        batch.setColor(bgColor);
        Drawable background = getSkin().getDrawable("white");
        background.draw(batch, getX()+1, getY()+1, getWidth()-2, getHeight()-2.5f);

        /// Shape renderer stuff

        batch.end();
        shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        drawLegend();

        drawCurve(batch, parentAlpha);

        shapeRenderer.end();
        batch.begin();
    }

    private void drawLegend() {
        shapeRenderer.setColor(gridColor);
        for(float i = 0; i < 1f; i+=0.1f) {
            drawLine(i, 0f, i, 1f, 1f);
            drawLine(0f, i, 1f, i, 1f);
        }
    }

    private void drawCurve(Batch batch, float parentAlpha) {
        tmp.set(3, 2);
        localToStageCoordinates(tmp);

        shapeRenderer.setColor(lineColor);

        if(curveDataProvider == null) return;

        Array<Vector2> points = curveDataProvider.getPoints();

        if(points == null) return;

        if(points.get(0).x > 0) {
            // draw a line from (0, v) to that point (u, v)
            drawLine(0, points.get(0).y, points.get(0).x, points.get(0).y);
        }

        for(int i = 0; i < points.size - 1; i++) {
            Vector2 from = points.get(i);
            Vector2 to = points.get(i+1);
            drawLine(from.x, from.y, to.x, to.y);
        }

        if(points.get(points.size-1).x < 1f) {
            // draw a line from that point(u,v) to (1, v)
            drawLine(points.get(points.size-1).x, points.get(points.size-1).y, 1f, points.get(points.size-1).y);
        }

        shapeRenderer.setColor(pointColor);
        // draw points
        for(int i = 0; i < points.size; i++) {
            Vector2 point = points.get(i);
            drawPoint(point.x, point.y);
        }
    }

    /**
     * values supplied from 0 to 1 range
     */
    private void drawLine(float x1, float y1, float x2, float y2) {
        drawLine(x1, y1, x2, y2, 2.5f);
    }

    private void drawLine(float x1, float y1, float x2, float y2, float thickness) {
        shapeRenderer.rectLine(tmp.x + x1 *width, tmp.y + y1 * height, tmp.x + x2 * width, tmp.y + y2 * height, thickness);
    }

    private void drawPoint(float x, float y) {
        shapeRenderer.circle(tmp.x + x * width, tmp.y + y * height, pointSize/1.5f);
    }

    public Skin getSkin() {
        return skin;
    }


    public void removePoint(int i) {
        if(points.size > 1) {
            points.removeIndex(i);
        }
    }

    public Array<Vector2> getPoints() {
        return points;
    }


    public int createPoint(float x, float y) {

        if(x < 0) x = 0;
        if(x > 1) x = 1;
        if(y < 0) y = 0;
        if(y > 1) y = 1;

        Vector2 point = new Vector2(x, y);
        points.add(point);
        sortPoints();
        return points.indexOf(point, true);
    }

    private void sortPoints() {
        points.sort(comparator);
    }
}
