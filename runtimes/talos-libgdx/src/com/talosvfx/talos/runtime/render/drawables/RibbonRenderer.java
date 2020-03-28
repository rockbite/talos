package com.talosvfx.talos.runtime.render.drawables;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticleDrawable;

public class RibbonRenderer implements ParticleDrawable {

    Particle particleRef;
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
    ObjectMap<Particle, Polyline> polylineMap = new ObjectMap<>();
    Array<Particle> tmpArr = new Array<>();

    private Color tmpColor = new Color();

    public RibbonRenderer() {
        textureRegionDrawable = new TextureRegionDrawable();
        accumulator = new PointMemoryAccumulator();
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height, float rotation) {
        if(interpolationPointCount < 1) return;
        if(ribbonRegion == null) return;

        accumulator.update(particleRef, x, y);

        Polyline polyline = polyline();

        accumulator.setDrawLocations(particleRef, polyline.getPoints());

        polyline.draw(batch, ribbonRegion);

        tmpArr.clear();
        for(Particle key: polylineMap.keys()) {
            if(key.alpha == 1f) {
                tmpArr.add(key);
            }
        }
        for(int i = 0; i < tmpArr.size; i++) {
            if(polylineMap.containsKey(tmpArr.get(i))) {
                polylinePool.free(polylineMap.get(tmpArr.get(i)));
            }
            polylineMap.remove(tmpArr.get(i));
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
        if(polylineMap.get(particleRef) == null) {
            Polyline polyline = polylinePool.obtain();
            polyline.initPoints(interpolationPointCount);
            polylineMap.put(particleRef, polyline);
        }

        return polylineMap.get(particleRef);
    }

    @Override
    public float getAspectRatio() {
        return textureRegionDrawable.getAspectRatio();
    }

    @Override
    public void setCurrentParticle (Particle particle) {
        this.particleRef = particle;
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
        float pointAlpha = accumulator.getPointAlpha(particleRef);
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

        ObjectMap<Particle, Vector2> leadPoints = new ObjectMap<>();
        ObjectFloatMap<Particle> leadLife = new ObjectFloatMap<>();
        ObjectMap<Particle, Array<Vector2>> allPoints = new ObjectMap<>();
        ObjectMap<Particle, Integer> pointCounts = new ObjectMap<>();

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

        public void clean(Particle particle) {
            leadPoints.remove(particle);
            leadLife.remove(particle, 0);
            allPoints.remove(particle);
            pointCounts.remove(particle);
        }

        private void initIfNull(Particle particle) {
            if(!leadPoints.containsKey(particle)) {
                leadPoints.put(particle, new Vector2());
                leadLife.put(particle, 0f);
                pointCounts.put(particle, 0);
                Array<Vector2> arr = new Array<>(pointCount - 1);
                allPoints.put(particle, arr);
                for(int i = 0; i < pointCount - 1; i++) {
                    arr.add(new Vector2());
                }

            }
        }

        public void update(Particle particle, float x, float y) {
            initIfNull(particle);

            float delta = Gdx.graphics.getDeltaTime();

            leadPoints.get(particle).set(x, y);

            leadLife.put(particle, leadLife.get(particle, 0f) + delta);

            if(leadLife.get(particle, 0f) > memoryDuration/pointCount) { // adding new point data
                Array<Vector2> points = allPoints.get(particle);
                int currPointCount = pointCounts.get(particle);
                if(currPointCount < pointCount - 1) {
                    currPointCount++;
                }
                pointCounts.put(particle, currPointCount);
                // now shift
                for(int i  = currPointCount - 1; i > 0; i--) {
                    points.get(i).set(points.get(i-1));
                }
                points.get(0).set(leadPoints.get(particle)); // set the value of lead point

                leadLife.put(particle, leadLife.get(particle, 0f) - memoryDuration/pointCount);
            }
        }

        public void setDrawLocations(Particle particle, Array<Polyline.PointData> points) {
            if(points != null && points.size == pointCount) {
                points.get(0).position.set(leadPoints.get(particle));

                for(int i = 0; i < points.size-1; i++) {

                    if(i >= 0 && i < pointCounts.get(particle)) {
                        Vector2 top = allPoints.get(particle).get(i);
                        Vector2 bottom = leadPoints.get(particle);
                        if(i > 0) {
                            bottom = allPoints.get(particle).get(i - 1);
                        }
                        tmpVec.set(bottom).sub(top).scl(leadLife.get(particle, 0f)/(memoryDuration/pointCount)).add(top);
                    } else {
                        tmpVec.set(allPoints.get(particle).get(i));
                    }

                    if(i < pointCounts.get(particle)) {
                        points.get(i + 1).position.set(tmpVec);
                    } else {
                        if(pointCounts.get(particle) > 0) {
                            points.get(i + 1).position.set(allPoints.get(particle).get(pointCounts.get(particle) - 1));
                        } else {
                            points.get(i + 1).color.a = 0;
                        }
                    }
                }
            }
        }

        public float getPointAlpha(Particle particle) {
            if(leadLife == null) return 0;
            return leadLife.get(particle, 0f)/(memoryDuration/pointCount);
        }
    }
}
