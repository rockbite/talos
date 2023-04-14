package com.talosvfx.talos.runtime.vfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.vfx.modules.DrawableModule;
import com.talosvfx.talos.runtime.vfx.modules.EmitterModule;
import com.talosvfx.talos.runtime.vfx.modules.ParticleModule;

public interface IEmitter {

    Array<ParticlePointGroup> pointData();

    void init();

    float getAlpha();
    ParticleModule getParticleModule();
    EmitterModule getEmitterModule();
    DrawableModule getDrawableModule();
    Vector3 getEffectPosition();

    int getEffectUniqueID ();
    ScopePayload getScope();
    Color getTint();

    void setScope(ScopePayload scope);
    int getActiveParticleCount();
    boolean isContinuous();
    boolean isComplete();
    void stop();
    void pause();
    void resume();
    void restart();

    void reset ();
    float getDelayRemaining();
    void update(float delta);
    ParticleEmitterDescriptor getEmitterGraph();
    void setVisible(boolean isVisible);
    boolean isVisible();
    boolean isAdditive();
    boolean isBlendAdd();
    Array<Particle> getActiveParticles();
}
