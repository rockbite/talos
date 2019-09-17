package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.rockbite.tools.talos.runtime.modules.ParticleModule;

public class Particle implements Pool.Poolable {

    public ParticleEmitter particleEmitter;

    Vector2 position = new Vector2();
    float life;
    float transparency;
    float rotation;
    float size;

    Color color = new Color();

    public float alpha; // alpha position from 0 to 1 in it's lifetime cycle

    public float seed;

    public float durationAtInit;

    public Particle() {
        // empty constructor
    }

    public void init(ParticleModule particleModule, ParticleEmitter particleEmitter) {
        this.particleEmitter = particleEmitter;

        this.seed = MathUtils.random();

        particleModule.updateScopeData(this);

        position.set(particleModule.getStartPosition());

        // inner variable defaults
        alpha = 0f;

        durationAtInit = particleEmitter.alpha;
    }

    public void update(float delta) {
        if(alpha == 1f) return;

        alpha += delta/life;
        if(alpha > 1f) alpha = 1f;

        //scope data
        ParticleModule particleModule = particleEmitter.moduleGraph.getParticleModule();
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
        rotation = particleModule.getRotation();
        size = particleModule.getSize();
        color.set(particleModule.getColor());

        // perform inner operations
        position.x += MathUtils.cosDeg(angle)*velocity*delta;
        position.y += MathUtils.sinDeg(angle)*velocity*delta;
    }

    @Override
    public void reset() {

    }
}
