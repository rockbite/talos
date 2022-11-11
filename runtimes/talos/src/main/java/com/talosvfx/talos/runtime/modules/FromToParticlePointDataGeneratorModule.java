package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticlePointData;
import com.talosvfx.talos.runtime.ParticlePointGroup;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.values.ModuleValue;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class FromToParticlePointDataGeneratorModule extends ParticlePointDataGeneratorModule {

	public static final int FROM = 1;
	public static final int TO = 2;
	public static final int POINTS_COUNT = 3;

	ModuleValue<ParticlePointDataGeneratorModule> outModule;

	NumericalValue from;
	NumericalValue to;
	NumericalValue pointCount;

	public static int defaultPoints = 10;

	@Override
	protected void defineSlots () {
		outModule = new ModuleValue<>();
		outModule.setModule(this);
		createOutputSlot(ParticlePointDataGeneratorModule.MODULE, outModule);

		from = createInputSlot(FROM);
		to = createInputSlot(TO);
		pointCount = createInputSlot(POINTS_COUNT);

		pointCount.set(defaultPoints);

	}

	@Override
	public void processCustomValues () {
	}

	@Override
	public void update (float delta) {

	}

	private Vector3 temp = new Vector3();
	private Vector3 temp2 = new Vector3();

	@Override
	protected void createPoints (Particle particle, Pool<ParticlePointData> particlePointDataPool, ParticlePointGroup group) {

		int cachedRequestMode = getScope().getRequestMode();
		int cachedRequesterID = getScope().getRequesterID();

		getScope().setCurrentRequestMode(ScopePayload.SUB_PARTICLE_ALPHA);

		int numPoints = (int)pointCount.get(0);
		if (numPoints > 0) {
			for (int i = 0; i < numPoints; i++) {
				ParticlePointData obtain = particlePointDataPool.obtain();

				temp.set(from.get(0), from.get(1), from.get(2));
				temp2.set(to.get(0), to.get(1), to.get(2));

				float alpha = (float)i/numPoints;


				temp.interpolate(temp2, alpha, Interpolation.linear);

				getScope().set(ScopePayload.SUB_PARTICLE_ALPHA, alpha);
				getScope().setCurrentRequesterID(getScope().newParticleRequester());

				obtain.setFromParticle(particle, temp);
				obtain.pointDataIndex = i;
				obtain.alpha = alpha;

				group.pointDataArray.add(obtain);
			}
		}

		getScope().setCurrentRequestMode(cachedRequestMode);
		getScope().setCurrentRequesterID(cachedRequesterID);
	}

	public void setDefaults (Vector3 from, Vector3 to) {
		this.from.set(from.x, from.y, from.z);
		this.to.set(to.x, to.y, to.z);
	}

	public void setNumPoints (float numPoints) {
		this.pointCount.set(numPoints);
	}

	@Override
	public void write (Json json) {
		super.write(json);
		json.writeValue("pointCount", pointCount.get(0));
		json.writeValue("fromX", from.get(0));
		json.writeValue("fromY", from.get(1));
		json.writeValue("fromZ", from.get(2));
		json.writeValue("toX", from.get(0));
		json.writeValue("toY", from.get(1));
		json.writeValue("toZ", from.get(2));
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		this.pointCount.set(0, jsonData.getInt("pointCount", defaultPoints));

		this.from.set(0, jsonData.getFloat("fromX", 0));
		this.from.set(1, jsonData.getFloat("fromY", 0));
		this.from.set(2, jsonData.getFloat("fromZ", 0));
		this.to.set(0, jsonData.getFloat("fromX", 0));
		this.to.set(1, jsonData.getFloat("fromY", 0));
		this.to.set(2, jsonData.getFloat("fromZ", 0));

	}

	public int getNumPoints () {
		return (int)pointCount.get(0);
	}
}
