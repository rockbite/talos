package com.talosvfx.talos.editor.render;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rockbite.bongo.engine.render.ShaderFlags;
import com.rockbite.bongo.engine.render.ShaderSourceProvider;
import com.rockbite.bongo.engine.render.SpriteShaderCompiler;

public class Render {

	private static Render instance;

	public static Render instance () {
		if (instance == null) {
			instance = new Render();
		}
		return instance;
	}

	private final ShapeRenderer shapeRenderer;

	public Render () {
		String shapeVertexSource = ShaderSourceProvider.resolveVertex("core/shape", Files.FileType.Classpath).readString();
		String shapeFragmentSource = ShaderSourceProvider.resolveFragment("core/shape", Files.FileType.Classpath).readString();

		shapeRenderer = new ShapeRenderer(5000,
			SpriteShaderCompiler.getOrCreateShader("core/shape", shapeVertexSource, shapeFragmentSource, new ShaderFlags())
		);
	}

	public ShapeRenderer shapeRenderer () {
		return shapeRenderer;
	}

	public void dispose () {
		shapeRenderer.dispose();
	}

}
