package com.talosvfx.talos.runtime.vfx.modules;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.runtime.vfx.Particle;
import com.talosvfx.talos.runtime.vfx.ParticlePointData;
import com.talosvfx.talos.runtime.vfx.ParticlePointGroup;
import com.talosvfx.talos.runtime.vfx.ScopePayload;
import com.talosvfx.talos.runtime.vfx.values.ModuleValue;
import com.talosvfx.talos.runtime.vfx.values.NumericalValue;

public class HistoryParticlePointDataGeneratorModule extends ParticlePointDataGeneratorModule {

	public static final int POINTS_COUNT = 0;
	public static final int MIN_DISTANCE = 1;

	ModuleValue<ParticlePointDataGeneratorModule> outModule;

	NumericalValue maxPoints;
	NumericalValue minDistance;

	public static int defaultMaxPoints = 20;
	public static float defaultMinDistanceBetweenPoints = 1;

	@Override
	protected void defineSlots () {
		outModule = new ModuleValue<>();
		outModule.setModule(this);
		createOutputSlot(ParticlePointDataGeneratorModule.MODULE, outModule);

		maxPoints = createInputSlot(POINTS_COUNT);
		minDistance = createInputSlot(MIN_DISTANCE);

		maxPoints.set(defaultMaxPoints);
		minDistance.set(defaultMinDistanceBetweenPoints);

	}

	@Override
	public void processCustomValues () {
	}

	@Override
	public void update (float delta) {

	}

	private Vector3 tempVec3 = new Vector3();

	private IntMap<Array<Vector3>> locationMap = new IntMap<>();

	private Pool<Vector3> vectorPool = new Pool<Vector3>() {
		@Override
		protected Vector3 newObject () {
			return new Vector3();
		}
	};

	private Pool<Array<Vector3>> arrayPool = new Pool<Array<Vector3>>() {
		@Override
		protected Array<Vector3> newObject () {
			return new Array<>();
		}
	};

	public void onParticleKilled (Particle particle) {
		if (locationMap.containsKey(particle.requesterID)) {
			Array<Vector3> remove = locationMap.remove(particle.requesterID);
			vectorPool.freeAll(remove);
			arrayPool.free(remove);
			remove.clear();
		} else {
			System.out.println("Not found history");
		}
	}

	@Override
	protected void createPoints (Particle particle, Pool<ParticlePointData> particlePointDataPool, ParticlePointGroup group) {

		int cachedRequestMode = getScope().getRequestMode();
		int cachedRequesterID = getScope().getRequesterID();

		if (!locationMap.containsKey(cachedRequesterID)) {
			locationMap.put(cachedRequesterID, arrayPool.obtain());
		}

		Array<Vector3> locationPoints = locationMap.get(cachedRequesterID);

		int numPoints = (int)maxPoints.get(0);
		if (numPoints > 0) {

			final boolean shouldPop = locationPoints.size == numPoints;

			boolean shouldAdd = false;

			if (locationPoints.size == 0) {
				shouldAdd = true;
			} else {
				final Vector3 newest = locationPoints.peek();
				final float dist = tempVec3.set(newest).dst(particle.getX(), particle.getY(), particle.getZ());
				if (dist > minDistance.getFloat()) {
					shouldAdd = true;
				}
			}


			if (shouldAdd) {
				if (shouldPop) {
					locationPoints.removeIndex(0);
				}
				locationPoints.add(vectorPool.obtain().set(particle.getX(), particle.getY(), particle.getZ()));
			}

		}

		getScope().setCurrentRequestMode(ScopePayload.SUB_PARTICLE_ALPHA);

		for (int i = 0; i < locationPoints.size; i++) {

			final ParticlePointData obtain = particlePointDataPool.obtain();

			float alpha = (float)i/locationPoints.size;

			getScope().set(ScopePayload.SUB_PARTICLE_ALPHA, alpha);
			getScope().setCurrentRequesterID(getScope().newParticleRequester());

			obtain.setFromParticle(particle, locationPoints.get(i), true);
			obtain.pointDataIndex = i;
			obtain.alpha = alpha;

			group.pointDataArray.add(obtain);

		}

		getScope().setCurrentRequestMode(cachedRequestMode);
		getScope().setCurrentRequesterID(cachedRequesterID);
	}


	public float getMinDistance () {
		return this.minDistance.get(0);
	}

	public int getMaxPoints () {
		return ((int)this.maxPoints.get(0));
	}


	public void setMinDistance (float minDistance) {
		this.minDistance.set(minDistance);
	}

	public void setMaxPoints (float maxPoints) {
		this.maxPoints.set(maxPoints);
	}

	@Override
	public void write (Json json) {
		super.write(json);
		json.writeValue("maxPoints", maxPoints.get(0));
		json.writeValue("minDistance", minDistance.get(0));
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		setMaxPoints(jsonData.getInt("maxPoints", defaultMaxPoints));
		setMinDistance(jsonData.getFloat("minDistance", defaultMinDistanceBetweenPoints));
	}
}
