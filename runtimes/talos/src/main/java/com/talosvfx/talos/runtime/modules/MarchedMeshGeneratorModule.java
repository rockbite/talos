package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticlePointData;
import com.talosvfx.talos.runtime.ParticlePointGroup;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.render.ParticleRenderer;
import com.talosvfx.talos.runtime.values.ModuleValue;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class MarchedMeshGeneratorModule extends MeshGeneratorModule {

	public static final int RADIUS = 1;

	ModuleValue<MarchedMeshGeneratorModule> outModule;

	NumericalValue radius;

	float scale = 0.125f;

	boolean render3D = false;

	@Override
	protected void defineSlots () {
		outModule = new ModuleValue<>();
		outModule.setModule(this);

		createOutputSlot(MeshGeneratorModule.MODULE, outModule);

		radius = createInputSlot(RADIUS);
		radius.set(1f, 1f);

	}

	@Override
	public void processCustomValues () {
	}

	void constructMesh () {
		if (this.render3D) {
		} else {
		}
	}

	@Override
	public void setRenderMode (boolean is3D) {
		if (this.render3D != is3D) {
			this.render3D = is3D;
			constructMesh();
		}
	}



	Vector3 upWorldSpace = new Vector3();
	Vector3 rightWorldSpace = new Vector3();

	Vector3 min = new Vector3();
	Vector3 max = new Vector3();

	Vector3 temp1 = new Vector3();
	Vector3 temp2 = new Vector3();
	Vector3 temp3 = new Vector3();

	@Override
	public void render (ParticleRenderer particleRenderer, MaterialModule materialModule, Array<ParticlePointGroup> pointData) {

		Camera camera = particleRenderer.getCamera();
		float[] viewValues = camera.view.val;

//		CameraRight_worldspace = {ViewMatrix[0][0], ViewMatrix[1][0], ViewMatrix[2][0]}
//		CameraUp_worldspace = {ViewMatrix[0][1], ViewMatrix[1][1], ViewMatrix[2][1]}

		float U = 0;
		float U2 = 1f;
		float V = 0f;
		float V2 = 1f;

		rightWorldSpace.set(viewValues[0], viewValues[4], viewValues[8]);
		upWorldSpace.set(viewValues[1], viewValues[5], viewValues[9]);

		temp1.setZero();
		temp2.setZero();
		temp3.setZero();

		min.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		max.set(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);

		//todo Temporary this module is in development
		ShapeRenderer shapeRenderer = new ShapeRenderer();
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.setColor(Color.WHITE);

		for (int i = 0; i < pointData.size; i++) {
			ParticlePointGroup particlePointGroup = pointData.get(i);

			Array<ParticlePointData> pointDataArray = particlePointGroup.pointDataArray;
			for (int j = 0; j < pointDataArray.size; j++) {
				ParticlePointData particlePointData = pointDataArray.get(j);

				Particle reference = particlePointData.reference;
				getScope().set(ScopePayload.SUB_PARTICLE_ALPHA, particlePointData.alpha);
				getScope().set(ScopePayload.PARTICLE_SEED, reference.seed);
				getScope().set(ScopePayload.PARTICLE_ALPHA, reference.alpha);

				getScope().setCurrentRequesterID(getScope().newParticleRequester());

				fetchInputSlotValue(RADIUS);

				float radius = this.radius.get(0);

				float x = particlePointData.x;
				float y = particlePointData.y;
				float z = particlePointData.z;

				min.set(Math.min(x - radius, min.x), Math.min(y - radius, min.y), Math.min(z - radius, min.z));
				max.set(Math.max(x + radius, max.x), Math.max(y + radius, max.y), Math.max(z + radius, max.z));


				shapeRenderer.box(x - radius, y - radius, z + radius, radius * 2, radius * 2, radius * 2);

			}
		}

		shapeRenderer.setColor(Color.RED);

		float boxWidth = Math.abs(max.x - min.x);
		float boxHeight = Math.abs(max.y - min.y);
		float boxDepth = Math.abs(max.z - min.z);
		shapeRenderer.line(min.x, min.y, min.z, min.x + boxWidth, min.y, min.z);
		shapeRenderer.line(min.x, min.y + boxHeight, min.z, min.x + boxWidth, min.y + boxHeight, min.z);

		shapeRenderer.line(min.x, min.y, min.z + boxDepth, min.x + boxWidth, min.y, min.z + boxDepth);
		shapeRenderer.line(min.x, min.y + boxHeight, min.z + boxDepth, min.x + boxWidth, min.y + boxHeight, min.z + boxDepth);

		shapeRenderer.setColor(Color.YELLOW);


		int nearestMultipleOfMinX = (int)((min.x - (min.x % scale))/scale);
		int nearestMultipleOfMinY = (int)((min.y - (min.y % scale))/scale);
		int nearestMultipleOfMinZ = (int)((min.z - (min.z % scale))/scale);

		int nearestMultipleOfMaxX = (int)((max.x - (max.x % scale))/scale);
		int nearestMultipleOfMaxY = (int)((max.y - (max.y % scale))/scale);
		int nearestMultipleOfMaxZ = (int)((max.z - (max.z % scale))/scale);

		for (int i = nearestMultipleOfMinX; i <= nearestMultipleOfMaxX; i++) {
			for (int j = nearestMultipleOfMinY; j <= nearestMultipleOfMaxY; j++) {
				for (int k = nearestMultipleOfMinZ; k <= nearestMultipleOfMaxZ; k++) {
					float x = i * scale;
					float y = j * scale;
					float z = k * scale;
					shapeRenderer.box(x - scale/2f, y - scale/2f, z + scale/2f, scale, scale, scale);
				}
			}
		}

		shapeRenderer.end();

		shapeRenderer.dispose();

	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		this.scale = jsonData.getFloat("scale", scale);

	}

	@Override
	public void write (Json json) {
		super.write(json);
		json.writeValue("scale", this.scale);
	}

	public void setScale (float scale) {
		this.scale = scale;
	}

	public float getScale () {
		return scale;
	}
}
