package com.rockbite.tools.talos.runtime.render.drawables;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import com.rockbite.tools.talos.runtime.ParticleDrawable;

public class PolylineRenderer implements ParticleDrawable {

    float seed;
    int interpolationPointCount;

    public Pool<Polyline> polylinePool = new Pool<Polyline>() {
        @Override
        protected Polyline newObject() {
            return new Polyline();
        }
    };
    private TextureRegion region;

    ObjectMap<Float, Polyline> polylineMap = new ObjectMap<>();
    ObjectMap<Float, Long> cacheExpire = new ObjectMap<>();
    Array<Float> tmpArr = new Array<>();

    @Override
    public void draw(Batch batch, float x, float y, float width, float height, float rotation) {
        Polyline polyline = polyline();
        polyline.set(width, rotation);
        polyline.draw(batch, region, x, y);

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
            polylinePool.free(polylineMap.get(tmpArr.get(i)));
            polylineMap.remove(tmpArr.get(i));
        }
    }

    @Override
    public float getAspectRatio() {
        return 1f;
    }

    @Override
    public void setSeed(float seed) {
        this.seed = seed;
    }

    public void setRegion(TextureRegion region) {
        this.region = region;
    }

    public void setCount(int count) {
        interpolationPointCount = count;
        // reset all existing items from the pool
        polylinePool.freeAll(polylineMap.values().toArray());
        polylineMap.clear();
    }

    public void setPointData(int pointIndex, float offsetX, float offsetY, float thickness, Color color) {
        Polyline polyline = polyline();
        polyline.setPointData(pointIndex, offsetX, offsetY, thickness, color);
    }

    private Polyline polyline() {
        if(polylineMap.get(seed) == null) {
            Polyline polyline = polylinePool.obtain();
            polyline.initPoints(interpolationPointCount);
            polylineMap.put(seed, polyline);
        }

        return polylineMap.get(seed);
    }

    public void setTangents(float leftX, float leftY, float rightX, float rightY) {
        Polyline polyline = polyline();
        polyline.leftTangent.set(leftX, leftY);
        polyline.rightTanget.set(rightX, rightY);
    }
}
