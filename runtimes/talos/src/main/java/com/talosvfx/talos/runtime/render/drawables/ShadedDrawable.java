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

public class ShadedDrawable extends ParticleDrawable {

    private ShaderProgram shaderProgram;

    private Texture texture;
    private TextureRegion region;
    private ObjectMap<String, TextureRegion> textureMap;

    private Color defaultUVOffset = new Color(0, 0, 1, 1);

    public ShadedDrawable() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        texture = new Texture(pixmap);
        region = new TextureRegion(texture);
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height, float rotation, float originX, float originY) {
    }

    @Override
    public void draw(Batch batch, Particle particle, Color color) {

        if (shaderProgram == null || !shaderProgram.isCompiled()) return;

        float rotation = particle.rotation.x;
        float width = particle.size.x;
        float height = particle.size.y;
        float y = particle.getY();
        float x = particle.getX();

        ShaderProgram prevShader = batch.getShader();

        batch.setShader(shaderProgram);

        shaderProgram = processShaderData(shaderProgram, particle.alpha * particle.life);

        batch.setColor(color);
        batch.draw(texture, x - width * particle.pivot.x, y - height * particle.pivot.y, width * particle.pivot.x, height * particle.pivot.y, width, height, 1f, 1f, rotation, 0, 0, 1, 1, false, false);
        batch.setShader(prevShader);
    }

    @Override
    public float getAspectRatio() {
        return 1f;
    }

    @Override
    public void setCurrentParticle(Particle particle) {

    }

    public ShaderProgram processShaderData(ShaderProgram shaderProgram, float time) {
        shaderProgram.setUniformf("u_time", time); // TODO this should be exposed as port later on

        if (textureMap != null) {
            int bind = 1;
            for (String uniformName : textureMap.keys()) {
                TextureRegion region = textureMap.get(uniformName);
                Texture texture = region.getTexture();
                texture.bind(bind);
                shaderProgram.setUniformi(uniformName, bind);

                defaultUVOffset.set(region.getU(), region.getV(), region.getU2(), region.getV2());

                shaderProgram.setUniformf(uniformName + "regionUV", defaultUVOffset); // todo: this needs some refactoring maybe

                Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
                bind++;
            }
        }

        return shaderProgram;
    }

    public ShaderProgram getShaderProgram(Batch batch, Color color, float alpha, float life) {
        if (shaderProgram == null || !shaderProgram.isCompiled()) return null;

        batch.setShader(shaderProgram);

        shaderProgram = processShaderData(shaderProgram, alpha * life);

        return shaderProgram;
    }

    @Override
    public TextureRegion getTextureRegion() {
        return region;
    }

    public void setShader(String fragCode) {
        if (fragCode == null) return;

        ShaderProgram.pedantic = true;

        shaderProgram = new ShaderProgram(
                DefaultShaders.DEFAULT_VERTEX_SHADER,
                fragCode
        );

        if(!shaderProgram.isCompiled()){
            Gdx.app.log("GL SHADER ERROR", shaderProgram.getLog());
        }
    }

    public void setTextures(ObjectMap<String, TextureRegion> textureMap) {
        this.textureMap = textureMap;
    }
}
