package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class TargetModule extends AbstractModule {

	public static final int FROM = 1;
	public static final int TO = 2;
	public static final int ALPHA_INPUT = 3;

	public static final int POSITION = 1;

	NumericalValue alphaInput;

	NumericalValue from;
	NumericalValue to;

	NumericalValue position;

	float defaultSpeed;

	public Vector3 defaultFrom = new Vector3();
	public Vector3 defaultTo = new Vector3();

	private Vector3 fromVecTmp = new Vector3();
	private Vector3 toVecTmp = new Vector3();

	@Override
	protected void defineSlots () {
		alphaInput = createInputSlot(ALPHA_INPUT);
		from = createInputSlot(FROM);
		to = createInputSlot(TO);

		position = createOutputSlot(POSITION);
	}

	@Override
	public void processCustomValues () {
		float alpha = 0;
		if (alphaInput.isEmpty()) {
			alpha = getScope().getFloat(ScopePayload.PARTICLE_ALPHA);
		} else {
			alpha = alphaInput.getFloat();
		}

		if (from.isEmpty()) {
			from.set(defaultFrom.x, defaultFrom.y, defaultFrom.z);
		}
		if (to.isEmpty()) {
			to.set(defaultTo.x, defaultTo.y, defaultTo.z);
		}



		// now the real calculation begins
		fromVecTmp.set(from.get(0), from.get(1), from.get(2));
		toVecTmp.set(to.get(0), to.get(1), to.get(2));

		toVecTmp.sub(fromVecTmp);

		float totalDistance = toVecTmp.len();
		toVecTmp.nor().scl(alpha * totalDistance).add(fromVecTmp);

		position.set(toVecTmp.x, toVecTmp.y, toVecTmp.z);
	}

	public void setDefaultPositions (Vector3 dFrom, Vector3 dTo) {
		defaultFrom.set(dFrom);
		defaultTo.set(dTo);
	}

	public void setDefaultSpeed (float velocity) {
		defaultSpeed = velocity;
	}

	public float getDefaultSpeed () {
		return defaultSpeed;
	}

	@Override
	public void write (Json json) {
		super.write(json);
		json.writeValue("speed", getDefaultSpeed());

		json.writeValue("fromX", defaultFrom.x);
		json.writeValue("fromY", defaultFrom.y);
		json.writeValue("fromZ", defaultFrom.z);
		json.writeValue("toX", defaultTo.x);
		json.writeValue("toY", defaultTo.y);
		json.writeValue("toZ", defaultTo.z);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		defaultSpeed = jsonData.getFloat("velocity", 0);

		defaultFrom.x = jsonData.getFloat("fromX", 0);
		defaultFrom.y = jsonData.getFloat("fromY", 0);
		defaultFrom.z = jsonData.getFloat("fromZ", 0);
		defaultTo.x = jsonData.getFloat("toX", 0);
		defaultTo.y = jsonData.getFloat("toY", 0);
		defaultTo.z = jsonData.getFloat("toZ", 0);
	}
}
