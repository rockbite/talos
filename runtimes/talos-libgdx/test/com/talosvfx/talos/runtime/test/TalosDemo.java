package com.talosvfx.talos.runtime.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFrame;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.ParticleEffectInstance;

public class TalosDemo extends ApplicationAdapter {

    private Viewport viewport;
    private PolygonSpriteBatch batch;
    private ParticleEffectInstance effect;

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 400;
        config.height = 400;
        config.title = "Talos Demo";
        LwjglFrame frame = new LwjglFrame(new TalosDemo(), config);
    }

    @Override
    public void create() {
        /**
         * We need a viewport for proper camerawork
         */
        viewport = new FitViewport(10f, 10f);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);

        /**
         * We may need polygon sprite batch to render more complex VFX such us beams
         */
        batch = new PolygonSpriteBatch();

        /**
         * Prepare the texture atlas.
         * Normally just load Texture Atlas,  but for the sake of demo this will be creating fake atlas from just one texture.
         */
        TextureRegion textureRegion = new TextureRegion(new Texture(Gdx.files.internal("fire.png")));
        TextureAtlas textureAtlas = new TextureAtlas();
        textureAtlas.addRegion("fire", textureRegion);

        /**
         * Creating particle effect instance from particle effect descriptor
         */
        ParticleEffectDescriptor effectDescriptor = new ParticleEffectDescriptor(Gdx.files.internal("fire.p"), textureAtlas);
        effect = effectDescriptor.createEffectInstance();
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        effect.update(delta);

        // now to render
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(viewport.getCamera().projection);

        batch.begin();
        effect.render(batch);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {

    }
}
