package com.talosvfx.talos.runtime;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.modules.DrawableModule;
import com.talosvfx.talos.runtime.modules.EmitterModule;
import com.talosvfx.talos.runtime.modules.ParticleModule;

public interface IEmitter {

    void init();

    float getAlpha();
    ParticleModule getParticleModule();
    EmitterModule getEmitterModule();
    DrawableModule getDrawableModule();
    Vector3 getEffectPosition();
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
    float getDelayRemaining();
    void update(float delta);
    ParticleEmitterDescriptor getEmitterGraph();
    void setVisible(boolean isVisible);
    boolean isVisible();
    boolean isAdditive();
    boolean isBlendAdd();
    Array<Particle> getActiveParticles();
}
