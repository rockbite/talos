package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticlePointData;
import com.talosvfx.talos.runtime.ParticlePointGroup;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.render.ParticleRenderer;
import com.talosvfx.talos.runtime.values.ModuleValue;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class StripMeshGeneratorModule extends MeshGeneratorModule {

	public static final int THICKNESS = 1;
	public static final int COLOUR = 2;
	public static final int TRANSPARENCY = 3;
	public static final int OFFSET = 4;

	ModuleValue<StripMeshGeneratorModule> outModule;

	NumericalValue thickness;
	NumericalValue colour;
	NumericalValue transparency;
	NumericalValue offset;


	//x,y,z,colour,u,v
	private int quadVertexSize3D = 3 + 1 + 2;

	//x,y,z,colour,u,v
	private int quadVertexSize2D = 2 + 1 + 2;
	private int quadVertCount = 4;

	private float[] verts = new float[quadVertCount * quadVertexSize2D];

	private boolean render3D;

	@Override
	protected void defineSlots () {
		outModule = new ModuleValue<>();
		outModule.setModule(this);
		createOutputSlot(MeshGeneratorModule.MODULE, outModule);

		thickness = createInputSlot(THICKNESS);
		thickness.set(1f);

		offset = createInputSlot(OFFSET);
		colour = createInputSlot(COLOUR);
		transparency = createInputSlot(TRANSPARENCY);

	}

	@Override
	public void processCustomValues () {
	}


	private Color fromColour = new Color();
	private Color toColour = new Color();
	private Color tempColour = new Color();

	private float fromTransparency;
	private float toTransparency;

	private Vector3 fromOffset = new Vector3();
	private Vector3 toOffset = new Vector3();

	Vector2 targetVector = new Vector2();
	Vector2 leftBase = new Vector2();
	Vector2 leftTarget = new Vector2();
	Vector2 rightBase = new Vector2();
	Vector2 rightTarget = new Vector2();

	void constructMesh () {
		if (this.render3D) {
			verts = new float[quadVertCount * quadVertexSize3D];
		} else {
			verts = new float[quadVertCount * quadVertexSize2D];
		}
	}

	@Override
	public void setRenderMode (boolean is3D) {
		if (this.render3D != is3D) {
			this.render3D = is3D;
			constructMesh();
		}
	}


	@Override
	public void render (ParticleRenderer particleRenderer, MaterialModule materialModule, Array<ParticlePointGroup> groupData) {

		for (int i = 0; i < groupData.size; i++) {
			ParticlePointGroup particlePointGroup = groupData.get(i);

			Array<ParticlePointData> pointData = particlePointGroup.pointDataArray;

			if (pointData.size < 2)
				return; //Nothing to render

			for (int j = 0; j < pointData.size - 1; j++) {
				ParticlePointData particlePointData = pointData.get(j);
				ParticlePointData nextParticlePointData = pointData.get(j + 1);

				Particle fromReference = particlePointData.reference;
				Particle toReference = nextParticlePointData.reference;

				getScope().set(ScopePayload.SUB_PARTICLE_ALPHA, particlePointData.alpha);
				getScope().set(ScopePayload.PARTICLE_SEED, fromReference.seed);
				getScope().setCurrentRequesterID(getScope().newParticleRequester());

				fetchAllInputSlotValues();
				float fromThicknessValue = thickness.get(0);
				fromOffset.set(offset.get(0), offset.get(1), offset.get(2));
				fromColour.set(colour.get(0), colour.get(1), colour.get(2), 1f);
				fromTransparency = transparency.getFloat();

				getScope().set(ScopePayload.SUB_PARTICLE_ALPHA, nextParticlePointData.alpha);
				getScope().set(ScopePayload.PARTICLE_SEED, toReference.seed);
				getScope().setCurrentRequesterID(getScope().newParticleRequester());

				fetchAllInputSlotValues();
				float nextThicknessValue = thickness.get(0);
				toOffset.set(offset.get(0), offset.get(1), offset.get(2));
				toColour.set(colour.get(0), colour.get(1), colour.get(2), 1f);
				toTransparency = transparency.getFloat();

				float alpha = (float)j / pointData.size;
				float nextAlpha = (float)(j + 1) / pointData.size;

				targetVector.set(nextParticlePointData.x, nextParticlePointData.y).sub(particlePointData.x, particlePointData.y).nor();

				leftBase.set(targetVector).rotate(90).scl(fromThicknessValue).add(particlePointData.x, particlePointData.y);
				rightBase.set(targetVector).rotate(-90).scl(fromThicknessValue).add(particlePointData.x, particlePointData.y);

				leftTarget.set(targetVector).rotate(90).scl(nextThicknessValue).add(nextParticlePointData.x, nextParticlePointData.y);
				rightTarget.set(targetVector).rotate(-90).scl(nextThicknessValue).add(nextParticlePointData.x, nextParticlePointData.y);

				leftBase.add(fromOffset.x, fromOffset.y);
				rightBase.add(fromOffset.x, fromOffset.y);
				rightTarget.add(toOffset.x, toOffset.y);
				leftTarget.add(toOffset.x, toOffset.y);

				fromColour.a = fromTransparency;
				toColour.a = toTransparency;

				float fromColourBits = fromColour.toFloatBits();
				float toColourBits = toColour.toFloatBits();

				//get uvs from material

				float U = alpha;
				float U2 = nextAlpha;
				float V = 0f;
				float V2 = 1f;

				int idx = 0;

				verts[idx++] = rightBase.x; // x1
				verts[idx++] = rightBase.y; // y1
				if (render3D)
					verts[idx++] = 0;
				verts[idx++] = fromColourBits;
				verts[idx++] = U; // u1
				verts[idx++] = V; // v1

				verts[idx++] = rightTarget.x; // x2
				verts[idx++] = rightTarget.y; // y2
				if (render3D)
					verts[idx++] = 0;
				verts[idx++] = toColourBits;
				verts[idx++] = U2; // u2
				verts[idx++] = V; // v2

				verts[idx++] = leftTarget.x; // x3
				verts[idx++] = leftTarget.y; // y2
				if (render3D)
					verts[idx++] = 0;
				verts[idx++] = toColourBits;
				verts[idx++] = U2; // u3
				verts[idx++] = V2; // v3

				verts[idx++] = leftBase.x; // x3
				verts[idx++] = leftBase.y; // y2
				if (render3D)
					verts[idx++] = 0;
				verts[idx++] = fromColourBits;
				verts[idx++] = U; // u3
				verts[idx++] = V2; // v3

				particleRenderer.render(verts, materialModule);

			}
		}
	}


}
