package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class AttractorModule extends AbstractModule {

    public static final int INITIAL_ANGLE = 0;
    public static final int INITIAL_VELOCITY = 1;
    public static final int ATTRACTOR_POSITION = 2; // maybe change to support multiple attractors later
    public static final int ALPHA = 3;

    public static final int ANGLE = 0;
    public static final int VELOCITY = 1;

    NumericalValue initialAngle;
    NumericalValue initialVelocity;
    NumericalValue attractorPosition;
    NumericalValue alpha;
    NumericalValue angle;
    NumericalValue velocity;

    Vector2 initialVector  = new Vector2();
    Vector2 attractionVector  = new Vector2();
    Vector2 pos = new Vector2();
    Vector2 result  = new Vector2();

    @Override
    protected void defineSlots() {
        initialAngle = createInputSlot(INITIAL_ANGLE);
        initialVelocity = createInputSlot(INITIAL_VELOCITY);
        attractorPosition = createInputSlot(ATTRACTOR_POSITION);
        alpha = createInputSlot(ALPHA);

        angle = createOutputSlot(ANGLE);
        velocity = createOutputSlot(VELOCITY);

        initialAngle.setFlavour(NumericalValue.Flavour.ANGLE);
    }

    @Override
    public void processCustomValues () {
        NumericalValue posNumVal = getScope().get(ScopePayload.PARTICLE_POSITION);
        pos.set(posNumVal.get(0), posNumVal.get(1));


        float alphaVal =  getScope().getFloat(ScopePayload.PARTICLE_ALPHA);;
        if(!alpha.isEmpty()) {
            alphaVal = alpha.getFloat();
        }

        initialVector.set(initialVelocity.getFloat(), 0);
        initialVector.rotate(initialAngle.getFloat());

        attractionVector.set(attractorPosition.get(0), attractorPosition.get(1)).sub(pos);
        attractionVector.nor().scl(initialVelocity.getFloat());

        Interpolation interpolation = Interpolation.linear;

        // now let's mix them
        result.set(interpolation.apply(initialVector.x, attractionVector.x, alphaVal),
                   interpolation.apply(initialVector.y, attractionVector.y, alphaVal));

        angle.set(result.angle());
        velocity.set(result.len());
    }
}
