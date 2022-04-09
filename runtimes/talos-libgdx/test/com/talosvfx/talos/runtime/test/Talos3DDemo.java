package com.talosvfx.talos.runtime.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rockbite.bongo.engine.render.AutoReloadingShaderProgram;
import com.rockbite.bongo.engine.render.ShaderSourceProvider;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.ParticleEffectInstance;
import com.talosvfx.talos.runtime.render.Particle3DRenderer;
import com.talosvfx.talos.runtime.render.p3d.Simple3DBatch;

public class Talos3DDemo extends ApplicationAdapter {

    private Viewport viewport;
    private PerspectiveCamera camera;
    private ParticleEffectInstance effect;

    private Particle3DRenderer defaultRenderer;

    private AutoReloadingShaderProgram shaderProgram;

    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(400, 400);
        config.setTitle("Talos 3D Demo");
        Lwjgl3Application application = new Lwjgl3Application(new Talos3DDemo(), config);
    }

    @Override
    public void create() {
        camera = new PerspectiveCamera(70, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport = new FitViewport(10f, 10f, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);


        TextureRegion textureRegion = new TextureRegion(new Texture(Gdx.files.internal("fire.png")));
        TextureAtlas textureAtlas = new TextureAtlas();
        textureAtlas.addRegion("fire", textureRegion);

        /**
         * Creating particle effect instance from particle effect descriptor
         */
        ParticleEffectDescriptor effectDescriptor = new ParticleEffectDescriptor(Gdx.files.internal("fire.p"), textureAtlas);
        effect = effectDescriptor.createEffectInstance();

        defaultRenderer = new Particle3DRenderer(camera);

        shaderProgram = new AutoReloadingShaderProgram(ShaderSourceProvider.resolveVertex("core/particle", Files.FileType.Classpath), ShaderSourceProvider.resolveFragment("core/particle", Files.FileType.Classpath));

    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        effect.update(delta);

        // now to render
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        final Simple3DBatch batch = defaultRenderer.getBatch();
        batch.begin(camera, shaderProgram.getShaderProgram());
        effect.render(defaultRenderer);
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
