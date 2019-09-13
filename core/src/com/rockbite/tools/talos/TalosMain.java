package com.rockbite.tools.talos;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.rockbite.tools.talos.editor.MainStage;

public class TalosMain extends ApplicationAdapter {
	MainStage mainStage;

	@Override
	public void create () {
		mainStage = new MainStage();
	}


	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		mainStage.act();
		mainStage.draw();
	}

	public void resize (int width, int height) {
		mainStage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose () {
		mainStage.dispose();
	}
}
