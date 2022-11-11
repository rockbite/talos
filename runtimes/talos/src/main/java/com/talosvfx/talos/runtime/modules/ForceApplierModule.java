package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class ForceApplierModule extends AbstractModule {

    public static final int SUM_FORCES = 0;
    public static final int ANGLE = 0;
    public static final int VELOCITY = 1;

    NumericalValue sumForces;
    NumericalValue angleOutput;
    NumericalValue velocityOutput;

    Vector2 tmp = new Vector2();

    @Override
    protected void defineSlots() {
        sumForces = createInputSlot(SUM_FORCES);

        angleOutput = createOutputSlot(ANGLE);
        velocityOutput = createOutputSlot(VELOCITY);
    }

    @Override
    public void processCustomValues () {
        float particleAlpha = getScope().getFloat(ScopePayload.PARTICLE_ALPHA);
        if(getScope().currParticle() == null) return;

        float particleLife = getScope().currParticle().life;
        float timePassed = particleAlpha * particleLife;

        tmp.set(sumForces.get(0), sumForces.get(1));

        float angle = tmp.angle();

        float velocity = tmp.len() * timePassed; // V = F * T for mass 1;

        angleOutput.set(angle);
        velocityOutput.set(velocity);
    }
}
