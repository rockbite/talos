package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.rockbite.tools.talos.runtime.modules.ParticleModule;

public class Particle implements Pool.Poolable {

    public ParticleEmitterInstance particleEmitter;

    public Vector2 spawnPosition = new Vector2();
    public Vector2 position = new Vector2();
    public float life;
    public float transparency;
    public float rotation;
    public Vector2 size = new Vector2();

    public Color color = new Color();

    public float alpha; // alpha position from 0 to 1 in it's lifetime cycle

    public float seed;

    public float durationAtInit;

    public ParticleDrawable drawable;

    public Particle() {
        // empty constructor
    }

    public void init(ParticleModule particleModule, ParticleEmitterInstance particleEmitter) {
        this.particleEmitter = particleEmitter;

        this.seed = MathUtils.random();

        particleModule.updateScopeData(this);

        position.set(particleModule.getStartPosition()); // offset
        spawnPosition.set(particleEmitter.getEffect().position);

        // inner variable defaults
        alpha = 0f;

        durationAtInit = particleEmitter.alpha;
    }

    public void update(float delta) {
        if(alpha == 1f) return;

        alpha += delta/life;
        if(alpha > 1f) alpha = 1f;

        //scope data
        ParticleModule particleModule = particleEmitter.emitterGraph.getParticleModule();
        if(particleModule == null) return;
        particleModule.updateScopeData(this);

        //update variable values
        Vector2 target = particleModule.getTarget();
        float angle = 0;
        if(target == null) {
            angle = particleModule.getAngle(); // do we take angle or target
        } else {
            angle = target.sub(position).angle();
        }

        float velocity = particleModule.getVelocity();
        life = particleModule.getLife();
        transparency = particleModule.getTransparency();

        if(particleEmitter.emitterGraph.emitterModule.isAligned()) {
            rotation = angle + particleModule.getRotation();
        } else {
            rotation = particleModule.getRotation();
        }

        drawable = particleModule.getDrawable(); // important to get drawable before size
        particleEmitter.emitterGraph.getScope().set(ScopePayload.DRAWABLE_ASPECT_RATIO, drawable.getAspectRatio());

        size.set(particleModule.getSize());
        Vector2 positionOverride = particleModule.getPosition();
        color.set(particleModule.getColor());

        // perform inner operations
        if(positionOverride != null) {
            position.set(positionOverride);
        } else {
            position.x += MathUtils.cosDeg(angle) * velocity * delta;
            position.y += MathUtils.sinDeg(angle) * velocity * delta;
        }
    }

    public float getX() {
        if(particleEmitter.isAttached()) {
            return particleEmitter.getEffect().position.x + position.x;
        } else {
            return spawnPosition.x + position.x;
        }
    }

    public float getY() {
        if(particleEmitter.isAttached()) {
            return particleEmitter.getEffect().position.y + position.y;
        } else {
            return spawnPosition.y + position.y;
        }
    }

    @Override
    public void reset() {

    }
}
