package com.talosvfx.talos.editor;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class FontTests extends ApplicationAdapter {


	private Stage stage;

	@Override
	public void create () {
		super.create();

		rebuild();

		//Gen fonts
	}

	public void rebuild () {
		System.out.println("Rebuilding");

		if (stage != null) {
			stage.dispose();
		}

		stage = new Stage(new FixedHeightViewport(1440, new OrthographicCamera()));

		Table table = new Table();
		table.setFillParent(true);
		stage.addActor(table);

		FreeTypeFontGenerator freeTypeFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonttest/Roboto-Black.ttf"));

		PixmapPacker packer = new PixmapPacker(4096, 4096, Pixmap.Format.RGBA8888, 2, true, new PixmapPacker.SkylineStrategy());
		String testCharacters = "abcdefg0123456789";

		float[] sizes = new float[]{0.02f, 0.03f, 0.04f, 0.05f, 0.1f, 0.2f, 0.2f};
		for (float size : sizes) {
			FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
			int fakePixelSize = MathUtils.floor(size * 1440f);
			int realPixelSize = MathUtils.floor(size * Gdx.graphics.getHeight());

			float scaling = (float)fakePixelSize / (float)realPixelSize;

			parameter.characters = testCharacters;

			parameter.size = realPixelSize;
			parameter.gamma = 1.8f;
			parameter.hinting = FreeTypeFontGenerator.Hinting.Full;

			parameter.packer = packer;
//			parameter.genMipMaps = true;

//			parameter.minFilter = Texture.TextureFilter.Linear;
//			parameter.magFilter = Texture.TextureFilter.Linear;

			BitmapFont bitmapFont = freeTypeFontGenerator.generateFont(parameter);
			bitmapFont.setUseIntegerPositions(false);
			bitmapFont.getData().setScale(scaling);

			Label.LabelStyle style = new Label.LabelStyle();
			style.font = bitmapFont;

			Label testLabel = new Label(size + " abcdefvwxyz", style) {
				@Override
				public void draw (Batch batch, float parentAlpha) {
					super.draw(batch, parentAlpha);
				}
			};

			testLabel.setAlignment(Align.right);
			table.add(testLabel).right();
			table.row();
		}


		stage.setDebugAll(true);
	}

	@Override
	public void render () {
		ScreenUtils.clear(0.5f, 0.5f, 0.5f, 1);

		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			rebuild();
		}

		stage.act();
		stage.draw();
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	public static void main (String[] args) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(1280 , 720);
		new Lwjgl3Application(new FontTests(), config);
	}
}
