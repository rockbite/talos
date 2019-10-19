package com.rockbite.tools.talos.runtime.render.drawables;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.TimeUtils;
import com.rockbite.tools.talos.runtime.ParticleDrawable;

public class PolylineDrawable implements ParticleDrawable {

    TextureRegion region;

    // point cache for each particle (this drawable is reused by all particles in the emitter)
    ObjectMap<Float, Array<PolyPoint>> points = new ObjectMap<>();
    ObjectMap<Float, Long> cacheExpire = new ObjectMap<>(); // dirty hack
    Array<Float> tmpArr = new Array<>();

    int innerPointCount = 0;

    private float[] vertices;
    private short[] indexes;

    private float particle;

    /**
     * Temporary Vectors used for geometry calculations
     */
    private Vector2 tmp = new Vector2();
    private Vector2 tmp2 = new Vector2();
    private Vector2 tmp3 = new Vector2();
    private Vector2 point1 = new Vector2();
    private Vector2 point2 = new Vector2();
    private Vector2 point3 = new Vector2();

    Color tmpColor = new Color(Color.WHITE);

    public void setCount(int count) {
        innerPointCount = count;
    }

    public class PolyPoint {
        Vector2 position = new Vector2();
        private Vector2 offset = new Vector2();
        float pos = 0;
        float scale;
        Color color = new Color();
        float thickness = 0.1f;

        public void setPos(float p, float scale) {
            pos = p;
            this.scale = scale;
            updatePosition();
        }

        public void setOffset(float offsetX, float offsetY) {
            offset.set(offsetX, offsetY);
            updatePosition();
        }

        private void updatePosition() {
            position.set(-scale + 2f * scale * pos + offset.x, offset.y);
        }
    }

    public PolylineDrawable() {
        region = new TextureRegion(new Texture(Gdx.files.internal("white.png")));
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height, float rotation) {
        if(batch instanceof PolygonSpriteBatch) {
            PolygonSpriteBatch polygonSpriteBatch = (PolygonSpriteBatch) batch;

            updateTranslation(x, y, width, rotation);

            for(int i = 0; i < innerPointCount + 2 - 1; i++) {
                tmpColor.set(batch.getColor());
                tmpColor.mul(getPoint(particle, i).color);

                // extrude each point
                extrudePoint(i, 0);
                extrudePoint(i, 1);

                // creating indexes
                indexes[i * 6] =     (short) (i * 4);
                indexes[i * 6 + 1] = (short) (i * 4 + 1);
                indexes[i * 6 + 2] = (short) (i * 4 + 3);
                indexes[i * 6 + 3] = (short) (i * 4);
                indexes[i * 6 + 4] = (short) (i * 4 + 3);
                indexes[i * 6 + 5] = (short) (i * 4 + 2);

            }

            // do the actual drawing
            polygonSpriteBatch.draw(region.getTexture(), vertices, 0, vertices.length, indexes, 0, indexes.length);


            // remove items from cache
            long timeNow = TimeUtils.millis();
            tmpArr.clear();
            for(Float seed: cacheExpire.keys()) {
                if(timeNow - cacheExpire.get(seed) > 200f) {
                    tmpArr.add(seed);
                }
            }
            for(int i = 0; i < tmpArr.size; i++) {
                cacheExpire.remove(tmpArr.get(i));
                points.remove(tmpArr.get(i));
            }
        }
    }

    private void updateTranslation(float x, float y, float width, float rotation) {
        for(int i = 0; i < innerPointCount + 2; i++) {
            // initial position
            getPoint(particle, i).setPos((float)i/(innerPointCount + 2 - 1), width/2f);

            // now apply offsets

            // now rotate
            getPoint(particle, i).position.rotate(rotation);

            // now translate
            getPoint(particle, i).position.add(x, y);
        }
    }

    private void extrudePoint(int index, int pos) {

        int i = index + pos;
        float v = pos;
        float u = (float)index/(innerPointCount+2-1);
        float u1 = (float)(index+1)/(innerPointCount+2-1);

        //u = 0;
        //u1 = 1;

        float thickness = getPoint(particle, i).thickness;

        if(i > 0 && i < innerPointCount + 2 - 1) {
            point1.set(getPoint(particle, i - 1).position);
            point2.set(getPoint(particle, i).position);
            point3.set(getPoint(particle, i + 1).position);

            tmp.set(point2).sub(point1).nor().rotate90(1).nor(); //Left hand side normal of first edge
            tmp2.set(point3).sub(point2).nor().rotate90(1).nor(); //Left hand side normal of second edge
            tmp3.set(tmp).add(tmp2).nor(); //Bisector
            tmp3.scl(thickness/2f);
            tmp.set(tmp3).add(point2);
            tmp3.scl(-1f);
            tmp2.set(tmp3).add(point2);

            packVertex(region, vertices, index * 4 + 1 + pos * 2, tmp.x, tmp.y, tmpColor, u, v); // left extension vertex
            packVertex(region, vertices, index * 4 + pos * 2, tmp2.x, tmp2.y, tmpColor, u1, v); // right extension vertex
        } else {
            if(i == 0) {
                point1.set(getPoint(particle, i).position);
                point2.set(getPoint(particle, i+1).position);

                final Vector2 nor = tmp.set(point2).sub(point1).nor();

                tmp2.set(nor).rotate90(1).scl(thickness/2f);
                tmp3.set(nor).rotate90(-1).scl(thickness/2f);

                tmp.set(tmp2).add(point1);
                tmp2.set(tmp3).add(point1);

                packVertex(region, vertices,index * 4 + 1 + pos * 2, tmp.x, tmp.y, tmpColor, 0, v); // left extension vertex
                packVertex(region, vertices, index * 4 + pos * 2, tmp2.x, tmp2.y, tmpColor, u1, v); // right extension vertex
            }
            if(i == innerPointCount + 2 - 1) {
                point1.set(getPoint(particle, i - 1).position);
                point2.set(getPoint(particle, i).position);

                final Vector2 nor = tmp.set(point2).sub(point1).nor();

                tmp2.set(nor).rotate90(1).scl(thickness/2f);
                tmp3.set(nor).rotate90(-1).scl(thickness/2f);

                tmp.set(tmp2).add(point2);
                tmp2.set(tmp3).add(point2);

                packVertex(region, vertices,index * 4 + 1 + pos * 2, tmp.x, tmp.y, tmpColor, u, v); // left extension vertex
                packVertex(region, vertices, index * 4 + pos * 2 , tmp2.x, tmp2.y, tmpColor, 1, v); // right extension vertex
            }
        }
    }

    private void packVertex(TextureRegion region, float[] vertices, int index, float x, float y, Color color, float u, float v) {
        float insideOffset = 0.0f; // needed in case region has have some weird transparent edge. maybe.

        vertices[index * 5] = x;
        vertices[index * 5 + 1] = y;
        vertices[index * 5 + 2] = color.toFloatBits();
        vertices[index * 5 + 3] = region.getU() + u * (region.getU2() - region.getU() - insideOffset) + insideOffset;
        vertices[index * 5 + 4] = region.getV() + v * (region.getV2() - region.getV() - insideOffset) + insideOffset;


    }

    public void setPointData(int i, float offsetX, float offsetY, float thickness, Color color) {
        getPoint(particle, i).setOffset(0, offsetY);
        getPoint(particle, i).color.set(color);
        getPoint(particle, i).thickness = thickness;
    }


    @Override
    public float getAspectRatio() {
        return 1f;
    }

    @Override
    public void setSeed(float seed) {
        particle = seed;
    }

    private PolyPoint getPoint(float particle, int index) {
        if(points.get(particle) == null) {
            points.put(particle, new Array<PolyPoint>());
        }
        if(points.get(particle).size != innerPointCount + 2) {
            resetInterpolationPoints(points.get(particle), innerPointCount);
        }

        cacheExpire.put(particle, TimeUtils.millis());

        return points.get(particle).get(index);
    }

    public void resetInterpolationPoints(Array<PolyPoint> array, float count) {
        array.clear();

        array.add(new PolyPoint());
        for(int i =  0; i < count; i++) {
            array.add(new PolyPoint());
        }
        array.add(new PolyPoint());

        int attributeCount = 5;
        int pointCount = array.size;
        int vertexCount = (pointCount - 1) * 4;
        int trisCount = (pointCount - 1) * 2;

        if(vertices == null || vertices.length != vertexCount * attributeCount) {
            vertices = new float[vertexCount * attributeCount];
            indexes = new short[trisCount * 3];
        }
    }
}
