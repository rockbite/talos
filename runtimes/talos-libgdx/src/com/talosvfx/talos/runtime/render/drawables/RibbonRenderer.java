package com.talosvfx.talos.runtime.render.drawables;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticleDrawable;

public class RibbonRenderer implements ParticleDrawable {

    float seed;
    int interpolationPointCount;

    PointMemoryAccumulator accumulator;

    TextureRegionDrawable textureRegionDrawable;
    TextureRegion ribbonRegion;

    public Pool<Polyline> polylinePool = new Pool<Polyline>() {
        @Override
        protected Polyline newObject() {
            return new Polyline();
        }
    };
    ObjectMap<Float, Polyline> polylineMap = new ObjectMap<>();
    ObjectMap<Float, Long> cacheExpire = new ObjectMap<>();
    Array<Float> tmpArr = new Array<>();

    private Color tmpColor = new Color();

    public RibbonRenderer() {
        textureRegionDrawable = new TextureRegionDrawable();
        accumulator = new PointMemoryAccumulator();
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height, float rotation) {
        if(interpolationPointCount < 1) return;
        if(ribbonRegion == null) return;

        accumulator.update(seed, x, y);

        Polyline polyline = polyline();

        accumulator.setDrawLocations(seed, polyline.getPoints());

        polyline.draw(batch, ribbonRegion);

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
            if(polylineMap.get(tmpArr.get(i)) != null) {
                polylinePool.free(polylineMap.get(tmpArr.get(i)));
            }
            polylineMap.remove(tmpArr.get(i));

            accumulator.clean(tmpArr.get(i)); // clean accumulator
        }
    }

    @Override
    public void draw(Batch batch, Particle particle, Color color) {
        float rotation = particle.rotation;
        float width = particle.size.x;
        float height = particle.size.y;
        float y = particle.getY();
        float x = particle.getX();

        draw(batch, x, y, width, height, rotation);
        textureRegionDrawable.draw(batch, particle, color);
    }

    private Polyline polyline() {
        if(polylineMap.get(seed) == null) {
            Polyline polyline = polylinePool.obtain();
            polyline.initPoints(interpolationPointCount);
            polylineMap.put(seed, polyline);
        }

        cacheExpire.put(seed, TimeUtils.millis());

        return polylineMap.get(seed);
    }

    @Override
    public float getAspectRatio() {
        return textureRegionDrawable.getAspectRatio();
    }

    @Override
    public void setSeed(float seed) {
        this.seed = seed;
    }


    @Override
    public TextureRegion getTextureRegion() {
        return ribbonRegion;
    }

    public void setRegions(TextureRegion mainRegion, TextureRegion ribbonRegion) {
        textureRegionDrawable.setRegion((Sprite) mainRegion);

        this.ribbonRegion = ribbonRegion;
    }

    public void setPointData(int pointIndex,float thickness, Color color) {
        Polyline polyline = polyline();
        polyline.setPointData(pointIndex, 0, 0, thickness, color);
    }

    public void adjustPointData() {
        float pointAlpha = accumulator.getPointAlpha(seed);
        Polyline polyline = polyline();
        for(int i = 1; i < polyline.points.size; i++) {
            float topThickness = polyline.points.get(i).thickness;
            float bottomThickness = polyline.points.get(i-1).thickness;
            Color topColor = polyline.points.get(i).color;
            Color bottomColor = polyline.points.get(i).color;

            tmpColor.set(topColor.r+(bottomColor.r-topColor.r)*pointAlpha,
                         topColor.g+(bottomColor.g-topColor.g)*pointAlpha,
                         topColor.b+(bottomColor.b-topColor.b)*pointAlpha,
                         topColor.a+(bottomColor.a-topColor.a)*pointAlpha);

            //polyline.setPointData(i, 0, 0, topThickness+(bottomThickness-topThickness)*pointAlpha, tmpColor);
        }
    }

    public void setConfig(int detail, float memoryDuration) {
        if(detail < 2) detail = 2;
        interpolationPointCount = detail - 2;
        accumulator.init(detail, memoryDuration);
        // reset all existing items from the pool
        polylinePool.freeAll(polylineMap.values().toArray());
        polylineMap.clear();
    }

    public class PointMemoryAccumulator {

        int pointCount;
        float memoryDuration;

        ObjectMap<Float, Vector2> leadPoints = new ObjectMap<>();
        ObjectMap<Float, Float> leadLife = new ObjectMap<>();
        ObjectMap<Float, Array<Vector2>> allPoints = new ObjectMap<>();
        ObjectMap<Float, Integer> pointCounts = new ObjectMap<>();

        Vector2 tmpVec = new Vector2();

        public PointMemoryAccumulator() {
        }

        public void init(int pointCount, float memoryDuration) {
            this.memoryDuration = memoryDuration;
            this.pointCount = pointCount;
            leadPoints.clear();
            leadLife.clear();
            allPoints.clear();
            pointCounts.clear();
        }

        public void clean(float id) {
            leadPoints.remove(id);
            leadLife.remove(id);
            allPoints.remove(id);
            pointCounts.remove(id);
        }

        private void initIfNull(float id) {
            if(!leadPoints.containsKey(id)) {
                leadPoints.put(id, new Vector2());
                leadLife.put(id, 0f);
                pointCounts.put(id, 0);
                Array<Vector2> arr = new Array<>(pointCount - 1);
                allPoints.put(id, arr);
                for(int i = 0; i < pointCount - 1; i++) {
                    arr.add(new Vector2());
                }

            }
        }

        public void update(float id, float x, float y) {
            initIfNull(id);

            float delta = Gdx.graphics.getDeltaTime();

            leadPoints.get(id).set(x, y);

            leadLife.put(id, leadLife.get(id) + delta);

            if(leadLife.get(id) > memoryDuration/pointCount) { // adding new point data
                Array<Vector2> points = allPoints.get(id);
                int currPointCount = pointCounts.get(id);
                if(currPointCount < pointCount - 1) {
                    currPointCount++;
                }
                pointCounts.put(id, currPointCount);
                // now shift
                for(int i  = currPointCount - 1; i > 0; i--) {
                    points.get(i).set(points.get(i-1));
                }
                points.get(0).set(leadPoints.get(id)); // set the value of lead point

                leadLife.put(id, leadLife.get(id) - memoryDuration/pointCount);
            }
        }

        public void setDrawLocations(float id, Array<Polyline.PointData> points) {
            if(points != null && points.size == pointCount) {
                points.get(0).position.set(leadPoints.get(id));

                for(int i = 0; i < points.size-1; i++) {

                    if(i >= 0 && i < pointCounts.get(id)) {
                        Vector2 top = allPoints.get(id).get(i);
                        Vector2 bottom = leadPoints.get(id);
                        if(i > 0) {
                            bottom = allPoints.get(id).get(i - 1);
                        }
                        tmpVec.set(bottom).sub(top).scl(leadLife.get(id)/(memoryDuration/pointCount)).add(top);
                    } else {
                        tmpVec.set(allPoints.get(id).get(i));
                    }

                    if(i < pointCounts.get(id)) {
                        points.get(i + 1).position.set(tmpVec);
                    } else {
                        if(pointCounts.get(id) > 0) {
                            points.get(i + 1).position.set(allPoints.get(id).get(pointCounts.get(id) - 1));
                        } else {
                            points.get(i + 1).color.a = 0;
                        }
                    }
                }
            }
        }

        public float getPointAlpha(float id) {
            if(leadLife == null || leadLife.get(id) == null) return 0;
            return leadLife.get(id)/(memoryDuration/pointCount);
        }
    }
}
