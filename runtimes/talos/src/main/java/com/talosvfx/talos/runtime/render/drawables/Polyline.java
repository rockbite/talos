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

package com.talosvfx.talos.runtime.render.drawables;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class Polyline implements Pool.Poolable {

    Vector2 leftTangent = new Vector2();
    Vector2 rightTanget = new Vector2();

    Vector2 leftPoint = new Vector2();
    Vector2 rightPoint = new Vector2();

    float rotation;

    Array<PointData> points = new Array<>();

    // inner workings
    private float[] vertices;
    private short[] indexes;

    private Color tmpColor = new Color(Color.WHITE);
    private Vector2 tmp = new Vector2();
    private Vector2 tmp2 = new Vector2();
    private Vector2 tmp3 = new Vector2();
    private Vector2 point0 = new Vector2();
    private Vector2 point1 = new Vector2();
    private Vector2 point2 = new Vector2();
    private Vector2 point3 = new Vector2();

    Batch batch;

    @Override
    public void reset() {
        for(PointData pointData: points) {
            pointData.position.set(0, 0);
            pointData.offset.set(0, 0);
            pointData.thickness = 0;
        }
    }

    class PointData {
        Vector2 position = new Vector2();
        Vector2 offset = new Vector2();
        float thickness;
        Color color = new Color();
    }

    public Polyline() {

    }

    public void set(float size, float rotation) {
        leftPoint.set(-size/2f, 0);
        rightPoint.set(size/2f, 0);
        this.rotation = rotation;
    }

    public void initPoints(int interpolationPoints, float x, float y) {
        if(points.size != interpolationPoints + 2) {
            points.clear();
            for(int i = 0; i < interpolationPoints + 2; i++) {
                points.add(new PointData());
            }
            initVertices();
        } // else we reuse them

        for(int i = 0; i < points.size; i++) {
            points.get(i).position.set(x, y);
        }

    }

    private void initVertices() {
        int attributeCount = 5;
        int pointCount = points.size;
        int vertexCount = (pointCount - 1) * 4;
        int trisCount = (pointCount - 1) * 2;

        if(vertices == null || vertices.length != vertexCount * attributeCount) {
            vertices = new float[vertexCount * attributeCount];
            indexes = new short[trisCount * 3];
        }
    }

    public void setPointData(int index, float offsetX, float offsetY, float thickness, Color color) {
        points.get(index).color.set(color);
        points.get(index).offset.set(offsetX, offsetY);
        points.get(index).thickness = thickness;
    }

    public Array<PointData> getPoints() {
        return points;
    }

    public void draw(Batch batch, TextureRegion region, ShaderProgram shaderProgram) {
        if(region == null) return;
        if(batch instanceof PolygonBatch) {
            PolygonBatch polygonSpriteBatch = (PolygonBatch) batch;
            this.batch = polygonSpriteBatch;

            for(int i = 0; i < points.size - 1; i++) {
                // extrude each point
                extrudePoint(region, i, 0);
                extrudePoint(region, i, 1);

                // creating indexes
                indexes[i * 6] =     (short) (i * 4);
                indexes[i * 6 + 1] = (short) (i * 4 + 1);
                indexes[i * 6 + 2] = (short) (i * 4 + 3);
                indexes[i * 6 + 3] = (short) (i * 4);
                indexes[i * 6 + 4] = (short) (i * 4 + 3);
                indexes[i * 6 + 5] = (short) (i * 4 + 2);

            }

            // do the actual drawing
            if(shaderProgram != null) {
                batch.setShader(shaderProgram);
            }
            polygonSpriteBatch.draw(region.getTexture(), vertices, 0, vertices.length, indexes, 0, indexes.length);
        }
    }

    public void draw(Batch batch, TextureRegion region, float x, float y, ShaderProgram shaderProgram) {
        if(region == null) return;

        if(batch instanceof PolygonBatch) {
            PolygonBatch polygonSpriteBatch = (PolygonBatch) batch;
            this.batch = polygonSpriteBatch;

            updatePointPositions(x, y);

            for(int i = 0; i < points.size - 1; i++) {
                // extrude each point
                extrudePoint(region, i, 0);
                extrudePoint(region, i, 1);

                // creating indexes
                indexes[i * 6] =     (short) (i * 4);
                indexes[i * 6 + 1] = (short) (i * 4 + 1);
                indexes[i * 6 + 2] = (short) (i * 4 + 3);
                indexes[i * 6 + 3] = (short) (i * 4);
                indexes[i * 6 + 4] = (short) (i * 4 + 3);
                indexes[i * 6 + 5] = (short) (i * 4 + 2);

            }

            // do the actual drawing
            if(shaderProgram != null) {
                batch.setShader(shaderProgram);
            }
            polygonSpriteBatch.draw(region.getTexture(), vertices, 0, vertices.length, indexes, 0, indexes.length);
        }
    }

    private void updatePointPositions(float x, float y) {
        for(int i = 0; i < points.size; i++) {
            float alpha = (float)i/(points.size-1);
            point0.set(leftPoint);
            point3.set(rightPoint);
            point1.set(leftPoint).add( leftTangent );
            point2.set(rightPoint).add(rightTanget );
            Bezier.cubic(points.get(i).position, alpha, point0, point1, point2, point3, tmp);

            // ad the offsets
            points.get(i).position.add(points.get(i).offset);

            // apply rotation while origin is at 0
            points.get(i).position.rotate(rotation);

            //apply origin position
            points.get(i).position.add(x, y);
        }
    }

    private void extrudePoint(TextureRegion region, int index, int pos) {

        int i = index + pos;
        float v = (float)(i)/(points.size-1);

        float thickness = points.get(i).thickness;

        if(i > 0 && i < points.size - 1) {
            point1.set(points.get(i-1).position);
            point2.set(points.get(i).position);
            point3.set(points.get(i+1).position);

            tmp.set(point2).sub(point1).nor().rotate90(1).nor(); //Left hand side normal of first edge
            tmp2.set(point3).sub(point2).nor().rotate90(1).nor(); //Left hand side normal of second edge
            tmp3.set(tmp).add(tmp2).nor(); //Bisector
            tmp3.scl(thickness/2f);
            tmp.set(tmp3).add(point2);
            tmp3.scl(-1f);
            tmp2.set(tmp3).add(point2);

            packVertex(region, vertices, index * 4 + 1 + pos * 2, tmp.x, tmp.y, points.get(i).color, 0, v); // left extension vertex
            packVertex(region, vertices, index * 4 + pos * 2, tmp2.x, tmp2.y, points.get(i + 1).color, 1, v); // right extension vertex
        } else {
            if(i == 0) {
                point1.set(points.get(i).position);
                point2.set(points.get(i+1).position);

                final Vector2 nor = tmp.set(point2).sub(point1).nor();

                tmp2.set(nor).rotate90(1).scl(thickness/2f);
                tmp3.set(nor).rotate90(-1).scl(thickness/2f);

                tmp.set(tmp2).add(point1);
                tmp2.set(tmp3).add(point1);

                packVertex(region, vertices,index * 4 + 1 + pos * 2, tmp.x, tmp.y, points.get(i).color, 0, v); // left extension vertex
                packVertex(region, vertices, index * 4 + pos * 2, tmp2.x, tmp2.y,  points.get(i + 1).color, 1, v); // right extension vertex
            }
            if(i == points.size - 1) {
                point1.set(points.get(i - 1).position);
                point2.set(points.get(i).position);

                final Vector2 nor = tmp.set(point2).sub(point1).nor();

                tmp2.set(nor).rotate90(1).scl(thickness/2f);
                tmp3.set(nor).rotate90(-1).scl(thickness/2f);

                tmp.set(tmp2).add(point2);
                tmp2.set(tmp3).add(point2);

                packVertex(region, vertices,index * 4 + 1 + pos * 2, tmp.x, tmp.y, points.get(i - 1).color, 0, v); // left extension vertex
                packVertex(region, vertices, index * 4 + pos * 2 , tmp2.x, tmp2.y, points.get(i).color, 1, v); // right extension vertex
            }
        }
    }


    private void packVertex(TextureRegion region, float[] vertices, int index, float x, float y, Color color, float u, float v) {
        float insideOffset = 0.0f; // needed in case region has have some weird transparent edge. maybe.

        tmpColor.set(color).mul(batch.getColor());

        vertices[index * 5] = x;
        vertices[index * 5 + 1] = y;
        vertices[index * 5 + 2] = tmpColor.toFloatBits();
        vertices[index * 5 + 3] = region.getU() + u * (region.getU2() - region.getU() - insideOffset) + insideOffset;
        vertices[index * 5 + 4] = region.getV() + v * (region.getV2() - region.getV() - insideOffset) + insideOffset;


    }

}
