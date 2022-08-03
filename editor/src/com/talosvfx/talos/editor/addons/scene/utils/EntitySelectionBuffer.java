package com.talosvfx.talos.editor.addons.scene.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.BufferUtils;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;

import java.nio.ByteBuffer;
import java.util.UUID;

public class EntitySelectionBuffer {

	private FrameBuffer frameBuffer;
	private final PolygonSpriteBatchMultiTexture customBatch;

	public EntitySelectionBuffer () {
		float scale = 1;
		frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, (int)(Gdx.graphics.getWidth() * scale), (int)(Gdx.graphics.getHeight() * scale), false);
		ShaderProgram shaderProgram = new ShaderProgram(Gdx.files.internal("shaders/entity/entity.vert.glsl"), Gdx.files.internal("shaders/entity/entity.frag.glsl"));
		customBatch = new PolygonSpriteBatchMultiTexture(10000, shaderProgram);
	}

	public void begin (Camera camera) {
		frameBuffer.begin();
		Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
		Gdx.gl.glClearColor(0, 0, 0, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
	}

	public void end () {
		frameBuffer.end();
		Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
	}

	public FrameBuffer getFrameBuffer () {
		return frameBuffer;
	}

	public PolygonSpriteBatchMultiTexture getCustomBatch () {
		return customBatch;
	}

	public void resize () { //todo resize the buffer
		frameBuffer.dispose();
		frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
	}

	public static Color getColourForEntityUUID (GameObject gameObject) {
		UUID uuid = gameObject.uuid;

		int hash = uuid.hashCode();

		Color color = new Color(hash);
		return color;
	}

	public Color getPixelAtNDC (Vector2 ndc) {
		int x = (int)Math.floor(ndc.x * frameBuffer.getWidth());
		int y = (int)Math.floor(ndc.y * frameBuffer.getHeight());

		frameBuffer.bind();

		Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
		final ByteBuffer pixels = BufferUtils.newByteBuffer(1 * 4);
		Gdx.gl.glReadPixels(x, y, 1, 1, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixels);

		frameBuffer.end();

		int r = pixels.get(0) % 256;
		int g = pixels.get(1) % 256;
		int b = pixels.get(2) % 256;
		int a = pixels.get(3) % 256;
		if (r < 0) r += 256;
		if (g < 0) g += 256;
		if (b < 0) b += 256;
		if (a < 0) a += 256;
		return new Color(r/256f, g/256f, b/256f, a/256f);
	}
}
