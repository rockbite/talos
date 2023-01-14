package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticleDrawable;
import com.talosvfx.talos.runtime.ParticlePointData;
import com.talosvfx.talos.runtime.ParticlePointGroup;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.render.ParticleRenderer;
import com.talosvfx.talos.runtime.values.DrawableValue;
import com.talosvfx.talos.runtime.values.ModuleValue;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class QuadMeshGeneratorModule extends MeshGeneratorModule {

	public static final int SIZE = 1;

	ModuleValue<QuadMeshGeneratorModule> outModule;

	NumericalValue size;

	//x,y,z,colour,u,v
	private int quadVertexSize3D = 3 + 1 + 2;

	//x,y,z,colour,u,v
	private int quadVertexSize2D = 2 + 1 + 2;
	private int quadVertCount = 6;

	private float[] verts = new float[quadVertCount * quadVertexSize2D];
	private short[] tris = new short[] {0, 1, 2, 3, 4, 5};

	boolean render3D = false;

	private boolean billboard;

	@Override
	protected void defineSlots () {
		outModule = new ModuleValue<>();
		outModule.setModule(this);

		createOutputSlot(MeshGeneratorModule.MODULE, outModule);

		size = createInputSlot(SIZE);
		size.set(1f, 1f);

	}

	@Override
	public void processCustomValues () {
	}

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

	private Color color = new Color();
	Vector3 p1 = new Vector3();
	Vector3 p2 = new Vector3();
	Vector3 p3 = new Vector3();
	Vector3 p4 = new Vector3();


	Vector3 tmp = new Vector3();
	Vector3 tmp2 = new Vector3();



	Vector3 upWorldSpace = new Vector3();
	Vector3 rightWorldSpace = new Vector3();

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

		if (materialModule instanceof SpriteMaterialModule) {
			DrawableValue drawableValue = ((SpriteMaterialModule)materialModule).getDrawableValue();
			ParticleDrawable drawable = drawableValue.getDrawable();
			TextureRegion textureRegion = drawable.getTextureRegion();
			U = textureRegion.getU();
			U2 = textureRegion.getU2();
			V = textureRegion.getV();
			V2 = textureRegion.getV2();
		}

		rightWorldSpace.set(viewValues[0], viewValues[4], viewValues[8]);
		upWorldSpace.set(viewValues[1], viewValues[5], viewValues[9]);

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

				fetchInputSlotValue(SIZE);

				float width = size.get(0);
				float height = size.get(1);
				if(size.elementsCount() == 1) {
					height = width;
				}

				int idx = 0;

				float x = particlePointData.x;
				float y = particlePointData.y;
				float z = particlePointData.z;

				ParticleModule particleModule = particlePointData.reference.getEmitter().getParticleModule();

				Vector3 rotation = reference.rotation;
				float transparency = particleModule.getTransparency();
				Color color = particleModule.getColor();

				this.color.set(color);
				this.color.a = transparency;

				float colourBits = this.color.toFloatBits();

				float halfWidth = width / 2f;
				float halfHeight = height / 2f;

				p1.set(-halfWidth, -halfHeight, 0);
				p2.set(halfWidth, -halfHeight, 0);
				p3.set(halfWidth, halfHeight, 0);
				p4.set(-halfWidth, halfHeight, 0);

				//Rotation probably AFTER billboard


				if (billboard) {

					//vec3 vertexPosition_worldspace =
					//    particleCenter_wordspace
					//    + CameraRight_worldspace * squareVertices.x * BillboardSize.x
					//    + CameraUp_worldspace * squareVertices.y * BillboardSize.y;

					tmp.set(rightWorldSpace).scl(-0.5f).scl(width);
					tmp2.set(upWorldSpace).scl(-0.5f).scl(height);
					p1.set(tmp).add(tmp2);

					tmp.set(rightWorldSpace).scl(0.5f).scl(width);
					tmp2.set(upWorldSpace).scl(-0.5f).scl(height);
					p2.set(tmp).add(tmp2);

					tmp.set(rightWorldSpace).scl(0.5f).scl(width);
					tmp2.set(upWorldSpace).scl(0.5f).scl(height);
					p3.set(tmp).add(tmp2);

					tmp.set(rightWorldSpace).scl(-0.5f).scl(width);
					tmp2.set(upWorldSpace).scl(0.5f).scl(height);
					p4.set(tmp).add(tmp2);

					//rotation matrix here is different, we want to do it around the cameras forward vector
					p1.rotate(rotation.x, camera.direction.x, camera.direction.y, camera.direction.z);
					p2.rotate(rotation.x, camera.direction.x, camera.direction.y, camera.direction.z);
					p3.rotate(rotation.x, camera.direction.x, camera.direction.y, camera.direction.z);
					p4.rotate(rotation.x, camera.direction.x, camera.direction.y, camera.direction.z);

				} else {

					if (particlePointData.reference.getEmitter().getEmitterModule().isAligned()) {
						Vector3 velocity = particlePointData.reference.velocity;
						float a = MathUtils.atan2(velocity.y, velocity.x) * MathUtils.radDeg;
						a += 90;
						p1.rotate(a, 0, 0, 1);
						p2.rotate(a, 0, 0, 1);
						p3.rotate(a, 0, 0, 1);
						p4.rotate(a, 0, 0, 1);
					} else {
						p1.rotate(rotation.x, 0, 0, 1);
						p2.rotate(rotation.x, 0, 0, 1);
						p3.rotate(rotation.x, 0, 0, 1);
						p4.rotate(rotation.x, 0, 0, 1);
					}
				}

				p1.add(x, y, z);
				p2.add(x, y, z);
				p3.add(x, y, z);
				p4.add(x, y, z);

				//get uvs from material



				verts[idx++] = p1.x; // x1
				verts[idx++] = p1.y; // y1
				if (render3D) {
					verts[idx++] = p1.z;
				}
				verts[idx++] = colourBits;
				verts[idx++] = U; // u1
				verts[idx++] = V; // v1


				verts[idx++] = p2.x; // x2
				verts[idx++] = p2.y; // y2
				if (render3D) {
					verts[idx++] = p2.z;
				}
				verts[idx++] = colourBits;
				verts[idx++] = U2; // u2
				verts[idx++] = V; // v2

				verts[idx++] = p3.x; // x3
				verts[idx++] = p3.y; // y2
				if (render3D) {
					verts[idx++] = p3.z;
				}
				verts[idx++] = colourBits;
				verts[idx++] = U2; // u3
				verts[idx++] = V2; // v3

				verts[idx++] = p3.x; // x3
				verts[idx++] = p3.y; // y2
				if (render3D) {
					verts[idx++] = p3.z;
				}
				verts[idx++] = colourBits;
				verts[idx++] = U2; // u3
				verts[idx++] = V2; // v3

				verts[idx++] = p4.x; // x3
				verts[idx++] = p4.y; // y2
				if (render3D) {
					verts[idx++] = p4.z;
				}
				verts[idx++] = colourBits;
				verts[idx++] = U; // u3
				verts[idx++] = V2; // v3

				verts[idx++] = p1.x; // x3
				verts[idx++] = p1.y; // y2
				if (render3D) {
					verts[idx++] = p1.z;
				}
				verts[idx++] = colourBits;
				verts[idx++] = U; // u1
				verts[idx++] = V; // v1

				particleRenderer.render(verts, verts.length, tris, tris.length, materialModule);

			}
		}

	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		this.billboard = jsonData.getBoolean("billboard", billboard);

	}

	@Override
	public void write (Json json) {
		super.write(json);
		json.writeValue("billboard", this.billboard);
	}

	public void setBillboard (boolean isBillboard) {
		billboard = isBillboard;
	}
}
