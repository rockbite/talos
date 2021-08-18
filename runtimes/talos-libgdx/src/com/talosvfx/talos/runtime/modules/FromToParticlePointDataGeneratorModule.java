package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticlePointData;
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
	public void processValues () {
	}

	@Override
	public void update (float delta) {

	}

	private Vector2 temp = new Vector2();
	private Vector2 temp2 = new Vector2();

	@Override
	protected void createPoints (Particle particle, Pool<ParticlePointData> particlePointDataPool) {

		int numPoints = (int)pointCount.get(0);
		if (numPoints > 0) {
			for (int i = 0; i < numPoints; i++) {
				ParticlePointData obtain = particlePointDataPool.obtain();

				temp.set(from.get(0), from.get(1));
				temp2.set(to.get(0), to.get(1));

				float alpha = (float)i/numPoints;

				temp.interpolate(temp2, alpha, Interpolation.linear);

				obtain.setFromParticle(particle, temp);
				obtain.pointDataIndex = i;

				pointData.add(obtain);
			}
		}

	}

	public void setDefaults (Vector2 from, Vector2 to) {
		this.from.set(from.x, from.y);
		this.to.set(to.x, to.y);

	}

	public void setNumPoints (float numPoints) {
		this.pointCount.set(numPoints);
	}
}
