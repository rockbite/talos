package com.talosvfx.talos.runtime.render.drawables;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticleDrawable;
import com.talosvfx.talos.runtime.utils.DefaultShaders;

public class ShadedDrawable implements ParticleDrawable {

    private ShaderProgram shaderProgram;

    private Texture texture;
    private TextureRegion region;
    private ObjectMap<String, Texture> textureMap;

    public ShadedDrawable() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        texture = new Texture(pixmap);
        region = new TextureRegion(texture);
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height, float rotation) {
    }

    @Override
    public void draw(Batch batch, Particle particle, Color color) {

        if (shaderProgram == null || !shaderProgram.isCompiled()) return;

        float rotation = particle.rotation;
        float width = particle.size.x;
        float height = particle.size.y;
        float y = particle.getY();
        float x = particle.getX();

        ShaderProgram prevShader = batch.getShader();

        batch.setShader(shaderProgram);

        shaderProgram.setUniformf("u_time", particle.alpha * particle.life); // TODO this should be exposed as port later on

        if (textureMap != null) {
            int bind = 1;
            for (String uniformName : textureMap.keys()) {
                Texture texture = textureMap.get(uniformName);
                texture.bind(bind);
                shaderProgram.setUniformi(uniformName, bind);
                Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
                bind++;
            }
        }

        batch.setColor(color);
        batch.draw(texture, x - width / 2f, y - height / 2f, width / 2f, height / 2f, width, height, 1f, 1f, rotation, 0, 0, 1, 1, false, false);
        batch.setShader(prevShader);
    }

    @Override
    public float getAspectRatio() {
        return 1f;
    }

    @Override
    public void setCurrentParticle(Particle particle) {

    }

    @Override
    public TextureRegion getTextureRegion() {
        return region;
    }

    public void setShader(String fragCode) {
        if (fragCode == null) return;

        ShaderProgram.pedantic = false;

        shaderProgram = new ShaderProgram(
                DefaultShaders.DEFAULT_VERTEX_SHADER,
                fragCode
        );

        if(!shaderProgram.isCompiled()){
            Gdx.app.log("GL SHADER ERROR", shaderProgram.getLog());
        }
    }

    public void setTextures(ObjectMap<String, Texture> textureMap) {
        this.textureMap = textureMap;
    }
}
