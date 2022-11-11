package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ShortArray;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticlePointData;
import com.talosvfx.talos.runtime.ParticlePointGroup;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.render.ParticleRenderer;
import com.talosvfx.talos.runtime.values.ModuleValue;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class StripMeshGeneratorModule extends MeshGeneratorModule {

	public static final int UVS = 1;
	public static final int COLOUR = 2;
	public static final int TRANSPARENCY = 3;
	public static final int OFFSET = 4;
	public static final int THICKNESS = 5;

	ModuleValue<StripMeshGeneratorModule> outModule;

	NumericalValue thickness;

	NumericalValue uvs;
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
		thickness.set(1);

		uvs = createInputSlot(UVS);
		uvs.set(1, 1);

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

	private static class Vertex {
		float x, y, z;
		float colour;
		float u, v;

		public void set (ParticlePointData particlePointData, Vector3 offset, float u, float v, float colourBits) {
			this.x = particlePointData.x + offset.x;
			this.y = particlePointData.y + offset.y;
			this.z = particlePointData.z + offset.z;
			this.colour = colourBits;
			this.u = u;
			this.v = v;
		}
	}

	@Override
	public void render (ParticleRenderer particleRenderer, MaterialModule materialModule, Array<ParticlePointGroup> groupData) {

		Array<Vertex> vertices = new Array<>();
		ShortArray tris = new ShortArray();

		for (int i = 0; i < groupData.size; i++) {
			ParticlePointGroup particlePointGroup = groupData.get(i);

			Array<ParticlePointData> pointData = particlePointGroup.pointDataArray;

			if (pointData.size < 2)
				return; //Nothing to render

			int vertIndex = 0;
			int triIndex = 0;

			Vector3 forward = new Vector3();
			Vector3 temp = new Vector3();

			float width = 1;
			Vector3 left = new Vector3();
			for (int j = 0; j < pointData.size; j++) {
				float progression = j / (float)(pointData.size - 1);

				ParticlePointData particlePointData = pointData.get(j);

				Particle fromReference = particlePointData.reference;

				float particleTransparency = fromReference.getEmitter().getParticleModule().getTransparency();

				getScope().set(ScopePayload.SUB_PARTICLE_ALPHA, particlePointData.alpha);
				getScope().set(ScopePayload.PARTICLE_SEED, fromReference.seed);
				getScope().set(ScopePayload.PARTICLE_ALPHA, fromReference.alpha);
				getScope().setCurrentRequesterID(getScope().newParticleRequester());

				fetchAllInputSlotValues();

				float scaleU = uvs.get(0);
				float scaleV = uvs.get(1);

				fromOffset.set(offset.get(0), offset.get(1), offset.get(2));
				fromColour.set(colour.get(0), colour.get(1), colour.get(2), 1f);
				fromTransparency = this.transparency.isEmpty() ? 1 : this.transparency.getFloat(); //default

				fromColour.a = fromTransparency * particleTransparency;

				float fromColourBits = fromColour.toFloatBits();

				//get uvs from material

				float U = scaleU;

				float leftBaseV = 0.5f + (0.5f * scaleV);
				float rightBaseV = 0.5f - (0.5f * scaleV);

				forward.setZero();
				if (j < pointData.size - 1) {
					//We are not at the end

					ParticlePointData nextParticlePointData = pointData.get(j + 1);

					temp.set(nextParticlePointData.x, nextParticlePointData.y, nextParticlePointData.z);
					temp.sub(particlePointData.x, particlePointData.y, particlePointData.z);

					forward.add(temp);
				}
				if (j > 0) {
					ParticlePointData prevParticlePointData = pointData.get(j - 1);

					temp.set(particlePointData.x, particlePointData.y, particlePointData.z);
					temp.sub(prevParticlePointData.x, prevParticlePointData.y, prevParticlePointData.z);

					forward.add(temp);
				}
				forward.nor();

				//2d hack,
				left.set(-forward.y, forward.x, forward.z);

				Vertex vertexL = new Vertex();
				Vertex vertexR = new Vertex();

				vertexL.set(particlePointData, temp.set(left).scl(width * 0.5f), U, leftBaseV, fromColourBits);
				vertexR.set(particlePointData, temp.set(left).scl(width * 0.5f).scl(-1), U, rightBaseV, fromColourBits);

				vertices.add(vertexL);
				vertices.add(vertexR);

				vertexL.x += fromOffset.x;
				vertexL.y += fromOffset.y;
				vertexL.z += fromOffset.z;

				vertexR.x += fromOffset.x;
				vertexR.y += fromOffset.y;
				vertexR.z += fromOffset.z;

				if (j < (pointData.size - 1)) {
					tris.add(vertIndex);
					tris.add(vertIndex + 2);
					tris.add(vertIndex + 1);

					tris.add(vertIndex + 1);
					tris.add(vertIndex + 2);
					tris.add(vertIndex + 3);
				}

				vertIndex += 2;
				triIndex += 6;
			}

			int perVertex = render3D ? 6 : 5;

			float[] verts = new float[tris.size * perVertex];

			int idx = 0;
			for (int tri = 0; tri < tris.size; tri++) {
				final short vertI = tris.get(tri);

				final Vertex vertex = vertices.get(vertI);

				verts[idx++] = vertex.x;
				verts[idx++] = vertex.y;
				if (render3D) {
					verts[idx++] = vertex.z;
				}
				verts[idx++] = vertex.colour;
				verts[idx++] = vertex.u;
				verts[idx++] = vertex.v;
			}

			final int triSize = tris.size;

			vertices.clear();
			tris.clear();


			//Fake it
			for (int triI = 0; triI < triSize; triI++) {
				tris.add(triI);
			}

			particleRenderer.render(verts, verts.length, tris.items, tris.size, materialModule);

			tris.clear();

		}
	}
}
