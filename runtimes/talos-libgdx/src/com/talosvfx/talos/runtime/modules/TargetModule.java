package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class TargetModule extends AbstractModule {

    public static final int VELOCITY = 0;
    public static final int FROM = 1;
    public static final int TO = 2;
    public static final int ALPHA_INPUT = 3;

    public static final int TIME = 0;
    public static final int POSITION = 1;
    public static final int VELOCITY_OUT = 2;
    public static final int ANGLE = 3;

    NumericalValue alphaInput;
    NumericalValue velocity;
    NumericalValue from;
    NumericalValue to;
    NumericalValue time;
    NumericalValue position;
    NumericalValue velocityOut;
    NumericalValue angle;

    float defaultVelocity;
    public Vector2 defaultFrom = new Vector2();
    public Vector2 defaultTo = new Vector2();

    private Vector2 fromVecTmp = new Vector2();
    private Vector2 toVecTmp = new Vector2();

    private Vector2 tmp = new Vector2();

    @Override
    protected void defineSlots() {
        alphaInput = createInputSlot(ALPHA_INPUT);
        velocity= createInputSlot(VELOCITY);
        from= createInputSlot(FROM);
        to= createInputSlot(TO);

        time = createOutputSlot(TIME);
        position = createOutputSlot(POSITION);
        velocityOut = createOutputSlot(VELOCITY_OUT);
        angle = createOutputSlot(ANGLE);
    }

    @Override
    public void processCustomValues () {
        float alpha = 0;
        if(alphaInput.isEmpty()) {
            alpha = getScope().getFloat(ScopePayload.PARTICLE_ALPHA);
        } else {
            alpha = alphaInput.getFloat();
        }

        if(velocity.isEmpty()) velocity.set(defaultVelocity);
        if(from.isEmpty()) from.set(defaultFrom.x, defaultFrom.y);
        if(to.isEmpty()) to.set(defaultTo.x, defaultTo.y);


        velocityOut.set(velocity);

        // now the real calculation begins
        fromVecTmp.set(from.get(0), from.get(1));
        toVecTmp.set(to.get(0), to.get(1));
        toVecTmp.sub(fromVecTmp);

        float timeVal = toVecTmp.len()/velocity.getFloat();
        time.set(timeVal);

        float angleVal = toVecTmp.angle();
        angle.set(angleVal);

        tmp.set(toVecTmp).scl(alpha).add(fromVecTmp);

        position.set(tmp.x, tmp.y);
    }

    public void setDefaultPositions(Vector2 dFrom, Vector2 dTo) {
        defaultFrom.set(dFrom);
        defaultTo.set(dTo);
    }

    public void setDefaultVelocity(float velocity) {
        defaultVelocity = velocity;
    }

    public float getDefaultVelocity() {
        return defaultVelocity;
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("velocity", getDefaultVelocity());

        json.writeValue("fromX", defaultFrom.x);
        json.writeValue("fromY", defaultFrom.y);
        json.writeValue("toX", defaultTo.x);
        json.writeValue("toY", defaultTo.y);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        defaultVelocity = jsonData.getFloat("velocity", 0);

        defaultFrom.x = jsonData.getFloat("fromX", 0);
        defaultFrom.y = jsonData.getFloat("fromY", 0);
        defaultTo.x = jsonData.getFloat("toX", 0);
        defaultTo.y = jsonData.getFloat("toY", 0);
    }
}
