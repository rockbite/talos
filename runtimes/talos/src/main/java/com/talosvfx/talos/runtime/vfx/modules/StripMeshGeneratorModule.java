package com.talosvfx.talos.runtime.vfx.modules;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ShortArray;
import com.talosvfx.talos.runtime.vfx.Particle;
import com.talosvfx.talos.runtime.vfx.ParticlePointData;
import com.talosvfx.talos.runtime.vfx.ParticlePointGroup;
import com.talosvfx.talos.runtime.vfx.ScopePayload;
import com.talosvfx.talos.runtime.vfx.render.ParticleRenderer;
import com.talosvfx.talos.runtime.vfx.values.ModuleValue;
import com.talosvfx.talos.runtime.vfx.values.NumericalValue;

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

	private float[] verts = new float[10000];

	private boolean render3D;
	private Vector3 left = new Vector3();
	private Vector3 temp = new Vector3();
	private Vector3 forward = new Vector3();

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

	private Pool<Vertex> vertexPool = new Pool<Vertex>() {
		@Override
		protected Vertex newObject () {
			return new Vertex();
		}
	};

	void constructMesh () {

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

	Array<Vertex> vertices = new Array<>();
	ShortArray tris = new ShortArray();

	@Override
	public void render (ParticleRenderer particleRenderer, MaterialModule materialModule, Array<ParticlePointGroup> groupData) {

		float BASEU = 0;
		float BASEU2 = 1f;
		float BASEV = 0f;
		float BASEV2 = 1f;

		if (materialModule instanceof SpriteMaterialModule) {
			TextureRegion textureRegion = ((SpriteMaterialModule)materialModule).getTextureRegion();
			BASEU = textureRegion.getU();
			BASEU2 = textureRegion.getU2();
			BASEV = textureRegion.getV();
			BASEV2 = textureRegion.getV2();
		}

		float UVWIDTH = BASEU2 - BASEU;
		float VHEIGHT = BASEV2 - BASEV;

		float HALFU = UVWIDTH/2f;
		float HALFV = VHEIGHT/2f;


		for (int i = 0; i < groupData.size; i++) {
			ParticlePointGroup particlePointGroup = groupData.get(i);

			Array<ParticlePointData> pointData = particlePointGroup.pointDataArray;


			if (pointData.size < 2)
				continue; //Nothing to render

			int vertIndex = 0;
			int triIndex = 0;

			for (int j = 0; j < pointData.size; j++) {
				float progression = j / (float)(pointData.size - 1);

				ParticlePointData particlePointData = pointData.get(j);

				Particle fromReference = particlePointData.reference;
                Vector2 worldScale = fromReference.getEmitter().getWorldScale();

				float particleTransparency = fromReference.getEmitter().getParticleModule().getTransparency();

				getScope().set(ScopePayload.SUB_PARTICLE_ALPHA, particlePointData.alpha);
				getScope().set(ScopePayload.PARTICLE_SEED, fromReference.seed);
				getScope().set(ScopePayload.PARTICLE_ALPHA, fromReference.alpha);
				getScope().setCurrentRequesterID(getScope().newParticleRequester());


				fetchAllInputSlotValues();

                float width = thickness.getFloat() * worldScale.x;

                float scaleU = uvs.get(0);
                float scaleV = uvs.get(1);

                fromOffset.set(offset.get(0), offset.get(1), offset.get(2));

                // Scale the offset by the world scale so relative positioning is maintained
                fromOffset.x *= worldScale.x;
                fromOffset.y *= worldScale.y;

				fromOffset.set(offset.get(0), offset.get(1), offset.get(2));
				fromColour.set(colour.get(0), colour.get(1), colour.get(2), 1f);
				fromTransparency = this.transparency.isEmpty() ? 1 : this.transparency.getFloat(); //default

				fromColour.a = fromTransparency * particleTransparency;

				float fromColourBits = fromColour.toFloatBits();

				//get uvs from material

				float U = BASEU + (UVWIDTH * scaleU * progression);

				float MIDV = BASEV + VHEIGHT * 0.5f;
				float leftBaseV = MIDV + (HALFV * scaleV);
				float rightBaseV = MIDV - (HALFV * scaleV);

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

				Vertex vertexL = vertexPool.obtain();
				Vertex vertexR = vertexPool.obtain();

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

			vertexPool.freeAll(vertices);
			vertices.clear();
			tris.clear();


			//Fake it
			for (int triI = 0; triI < triSize; triI++) {
				tris.add(triI);
			}

			particleRenderer.render(verts, idx, tris.items, tris.size, materialModule);

			tris.clear();

		}
	}
}
