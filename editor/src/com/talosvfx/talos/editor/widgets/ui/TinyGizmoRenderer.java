package com.talosvfx.talos.editor.widgets.ui;

import com.asidik.tinygizmo.GizmoState;
import com.asidik.tinygizmo.RigidTransform;
import com.asidik.tinygizmo.TinyGizmo;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.SharedLibraryLoader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class TinyGizmoRenderer {

	private final TinyGizmo tinyGizmo;
	private final Mesh mesh;
	private final ShaderProgram shaderProgram;
	private final RigidTransform rigidTransform;
	private final InputAdapter inputAdapter;

	private FloatBuffer vertices;
	private IntBuffer triangles;

	private final float[] verticesFloatArray;
	private final short[] trianglesShortArray;
	private Quaternion quaternion;

	private boolean interactGizmo;

	Color tempColor = new Color();


	public TinyGizmoRenderer () {
		tinyGizmo = new TinyGizmo();


		vertices = BufferUtils.newFloatBuffer(10000);
		triangles = BufferUtils.newIntBuffer(10000);
		verticesFloatArray = new float[10000];
		trianglesShortArray = new short[10000];

		mesh = new Mesh(false, 10000, 10000, new VertexAttributes(
			VertexAttribute.Position(),
			VertexAttribute.Normal(),
			VertexAttribute.ColorPacked()
		));

		shaderProgram = new ShaderProgram(Gdx.files.internal("shaders/gizmo/vert.glsl"), Gdx.files.internal("shaders/gizmo/frag.glsl"));
		if (!shaderProgram.isCompiled()) {
			System.out.println(shaderProgram.getLog());
		}

		rigidTransform = new RigidTransform();
		quaternion = new Quaternion();

		inputAdapter = new InputAdapter() {

			IntIntMap keyMap = new IntIntMap();

			{
				keyMap.put(Input.Keys.CONTROL_LEFT, TinyGizmo.KEY_LEFT_CONTROL);
				keyMap.put(Input.Keys.L, TinyGizmo.KEY_L);
				keyMap.put(Input.Keys.T, TinyGizmo.KEY_T);
				keyMap.put(Input.Keys.R, TinyGizmo.KEY_R);
				keyMap.put(Input.Keys.S, TinyGizmo.KEY_S);
				keyMap.put(Input.Keys.W, TinyGizmo.KEY_W);
				keyMap.put(Input.Keys.A, TinyGizmo.KEY_A);
				keyMap.put(Input.Keys.D, TinyGizmo.KEY_D);
			}

			@Override
			public boolean keyDown (int keycode) {
				if (keyMap.containsKey(keycode)) {
					final int gizmoKeyCode = keyMap.get(keycode, -1);
					if (gizmoKeyCode != -1) {
						tinyGizmo.onKeyDown(gizmoKeyCode, TinyGizmo.BUTTON_PUSH, 0);
					}
				}
				return super.keyDown(keycode);
			}

			@Override
			public boolean keyUp (int keycode) {
				if (keyMap.containsKey(keycode)) {
					final int gizmoKeyCode = keyMap.get(keycode, -1);
					if (gizmoKeyCode != -1) {
						tinyGizmo.onKeyDown(gizmoKeyCode, TinyGizmo.BUTTON_RELEASE, 0);
					}
				}
				return super.keyUp(keycode);
			}

			@Override
			public boolean touchDown (int screenX, int screenY, int pointer, int button) {
				if (button == 0) {
					tinyGizmo.onMouseButton(TinyGizmo.MOUSE_BUTTON_LEFT, TinyGizmo.BUTTON_PUSH, 0);
				}
				if (button == 1) {
					tinyGizmo.onMouseButton(TinyGizmo.MOUSE_BUTTON_RIGHT, TinyGizmo.BUTTON_PUSH, 0);
				}
				return super.touchDown(screenX, screenY, pointer, button);
			}

			@Override
			public boolean touchUp (int screenX, int screenY, int pointer, int button) {
				if (button == 0) {
					tinyGizmo.onMouseButton(TinyGizmo.MOUSE_BUTTON_LEFT, TinyGizmo.BUTTON_RELEASE, 0);
				}
				if (button == 1) {
					tinyGizmo.onMouseButton(TinyGizmo.MOUSE_BUTTON_RIGHT, TinyGizmo.BUTTON_RELEASE, 0);
				}
				return super.touchUp(screenX, screenY, pointer, button);
			}
		};


	}

	public void render (PerspectiveCamera camera) {
		final Ray pickRay = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());

		tinyGizmo.updateWindow(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		tinyGizmo.updateRay(pickRay.direction.x, pickRay.direction.y, pickRay.direction.z);

		tinyGizmo.updateCamera(
			camera.fieldOfView, camera.near, camera.far,
			camera.position.x, camera.position.y, camera.position.z,
			quaternion.x, quaternion.y, quaternion.z, quaternion.w
		);

		tinyGizmo.update(new GizmoState());
		interactGizmo = tinyGizmo.transformGizmo("TestGizmo", rigidTransform);
		int[] sizes = new int[2];
		tinyGizmo.obtainRender(vertices, triangles, sizes);

		int vertexCount = sizes[0];
		int indiciesCount = sizes[1];

		int vertexSize = 3 + 3 + 4;

		vertices.position(0);
		triangles.position(0);


		int idx = 0;
		for (int i = 0; i < vertexCount; i++) {
			final float x = vertices.get();
			final float y = vertices.get();
			final float z = vertices.get();

			final float nx = vertices.get();
			final float ny = vertices.get();
			final float nz = vertices.get();

			final float r = vertices.get();
			final float g = vertices.get();
			final float b = vertices.get();
			final float a = vertices.get();

			final float colourPacked = tempColor.set(r, g, b, a).toFloatBits();

			verticesFloatArray[idx++] = x;
			verticesFloatArray[idx++] = y;
			verticesFloatArray[idx++] = z;
			verticesFloatArray[idx++] = nx;
			verticesFloatArray[idx++] = ny;
			verticesFloatArray[idx++] = nz;
			verticesFloatArray[idx++] = colourPacked;
		}

		int triIndex = 0;
		for (int i = 0; i < indiciesCount; i++) {
			final short t1 = (short)triangles.get();
			trianglesShortArray[triIndex++] = t1;
		}

		mesh.setVertices(verticesFloatArray, 0, idx);
		mesh.setIndices(trianglesShortArray, 0, indiciesCount );


		shaderProgram.bind();
		shaderProgram.setUniformMatrix("u_projTrans", camera.combined);
		mesh.render(shaderProgram, GL20.GL_TRIANGLES);
	}
}
