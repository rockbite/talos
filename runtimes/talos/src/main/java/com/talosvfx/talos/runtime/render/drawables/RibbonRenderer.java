package com.talosvfx.talos.runtime.render.drawables;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticleDrawable;

public class RibbonRenderer extends ParticleDrawable {

    Particle particleRef;
    int interpolationPointCount;

    PointMemoryAccumulator accumulator;

    TextureRegionDrawable textureRegionDrawable;
    TextureRegion ribbonRegion;

    ShadedDrawable shadedDrawable;

    public Pool<Polyline> polylinePool = new Pool<Polyline>() {
        @Override
        protected Polyline newObject() {
            return new Polyline();
        }
    };
    ObjectMap<Particle, Polyline> polylineMap = new ObjectMap<>();

    private Color tmpColor = new Color();

    public RibbonRenderer() {

        textureRegionDrawable = new TextureRegionDrawable();
        accumulator = new PointMemoryAccumulator();
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height, float rotation, float originX, float originY) {
        if(interpolationPointCount < 1) return;
        if(ribbonRegion == null && shadedDrawable == null) return;

        accumulator.update(particleRef, x, y);

        Polyline polyline = polyline();

        accumulator.setDrawLocations(particleRef, polyline.getPoints());

        if(shadedDrawable != null) {
            ShaderProgram prevShader = batch.getShader();
            ShaderProgram shaderProgram = shadedDrawable.getShaderProgram(batch, Color.WHITE, particleRef.alpha, particleRef.life);
            ribbonRegion = shadedDrawable.getTextureRegion();
            polyline.draw(batch, ribbonRegion, shaderProgram);
            batch.setShader(prevShader);
        } else {
            polyline.draw(batch, ribbonRegion, null);
        }
    }

    @Override
    public void draw(Batch batch, Particle particle, Color color) {
        float rotation = particle.rotation.x;
        float width = particle.size.x;
        float height = particle.size.y;
        float y = particle.getY();
        float x = particle.getX();

        draw(batch, x, y, width, height, rotation, particle.pivot.x, particle.pivot.y);
        textureRegionDrawable.draw(batch, particle, color);
    }

    private Polyline polyline() {
        if(polylineMap.get(particleRef) == null) {
            Polyline polyline = polylinePool.obtain();
            polyline.initPoints(interpolationPointCount, particleRef.getX(), particleRef.getY());
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

    @Override
    public void notifyCreate(Particle particle) {

    }

    @Override
    public void notifyDispose(Particle particle) {
       accumulator.clean(particle);
       Polyline polyline = polylineMap.get(particle);
       if(polyline != null) {
           polylineMap.remove(particle);
           polylinePool.free(polyline);
       }
    }

    public void setShadedDrawable(ShadedDrawable drawable) {
        shadedDrawable = drawable;
    }

    public class PointMemoryAccumulator {

        int pointCount;
        float memoryDuration;

        private Pool<AccData> dataPool;
        private ObjectMap<Particle, AccData> dataMap = new ObjectMap<>();

        Vector2 tmpVec = new Vector2();

        class AccData implements Pool.Poolable {
            Vector2 leadPoint = new Vector2();
            float leadLife = 0;
            Array<Vector2> points = new Array<>();
            int pointCount = 0;

            public AccData(int pointMaxCount) {
                for(int i = 0; i < pointMaxCount - 1; i++) {
                    points.add(new Vector2());
                }
            }

            @Override
            public void reset() {
                leadPoint.set(0, 0);
                leadLife = 0;
                for(int i = 0; i < pointCount - 1; i++) {
                    points.get(i).set(0, 0);
                }
                pointCount = 0;
            }
        }

        public PointMemoryAccumulator() {

        }

        public void init(final int pointCount, float memoryDuration) {
            this.memoryDuration = memoryDuration;
            this.pointCount = pointCount;
            if(dataPool != null) {
                dataPool.clear();
            }

            dataPool = new Pool<AccData>() {
                @Override
                protected AccData newObject() {
                    return new AccData(pointCount);
                }
            };

            dataMap.clear();
        }

        public void clean(Particle particle) {
            AccData accData = dataMap.get(particle);
            if(accData != null) {
                dataPool.free(accData);
                dataMap.remove(particle);
            }
        }

        private void initIfNull(Particle particle) {
            if(!dataMap.containsKey(particle)) {
                AccData accData = dataPool.obtain();
                dataMap.put(particle, accData);
            }
        }

        public void update(Particle particle, float x, float y) {
            initIfNull(particle);

            float delta = Gdx.graphics.getDeltaTime();

            if(delta > 1f/60f) delta = 1f/60f;

            dataMap.get(particle).leadPoint.set(x, y);

            dataMap.get(particle).leadLife = dataMap.get(particle).leadLife + delta;

            if(dataMap.get(particle).leadLife > memoryDuration/pointCount) { // adding new point data
                Array<Vector2> points = dataMap.get(particle).points;
                int currPointCount = dataMap.get(particle).pointCount;

                if(currPointCount < pointCount - 1) {
                    currPointCount++;
                }
                dataMap.get(particle).pointCount = currPointCount;

                // now shift
                for(int i  = currPointCount - 1; i > 0; i--) {
                    points.get(i).set(points.get(i-1));
                }
                points.get(0).set(dataMap.get(particle).leadPoint); // set the value of lead point

                dataMap.get(particle).leadLife = dataMap.get(particle).leadLife - memoryDuration/pointCount;
            }
        }

        public void setDrawLocations(Particle particle, Array<Polyline.PointData> points) {
            if(points != null && points.size == pointCount) {
                points.get(0).position.set(dataMap.get(particle).leadPoint);

                if(dataMap.get(particle).pointCount == 0) {
                    for(int i = 0; i < points.size; i++) {
                        points.get(i).color.a = 0;
                        points.get(i).position.set(dataMap.get(particle).leadPoint);
                    }

                    return;
                }


                for(int i = 0; i < points.size-1; i++) {

                    if(i >= 0 && i < dataMap.get(particle).pointCount) {
                        Vector2 top = dataMap.get(particle).points.get(i);
                        Vector2 bottom = dataMap.get(particle).leadPoint;
                        if(i > 0) {
                            bottom = dataMap.get(particle).points.get(i - 1);
                        }
                        tmpVec.set(bottom).sub(top).scl(dataMap.get(particle).leadLife/(memoryDuration/pointCount)).add(top);
                    } else {
                        tmpVec.set(dataMap.get(particle).points.get(i));
                    }

                    if(i < dataMap.get(particle).pointCount) {
                        points.get(i + 1).position.set(tmpVec);
                    } else {
                        if(dataMap.get(particle).pointCount > 0) {
                            points.get(i + 1).position.set(dataMap.get(particle).points.get(dataMap.get(particle).pointCount - 1));
                        } else {
                            points.get(i + 1).color.a = 0;
                        }
                    }
                }
            }
        }

        public float getPointAlpha(Particle particle) {
            if(dataMap.get(particle) == null) return 0;

            return dataMap.get(particle).leadLife/(memoryDuration/pointCount);
        }
    }
}
