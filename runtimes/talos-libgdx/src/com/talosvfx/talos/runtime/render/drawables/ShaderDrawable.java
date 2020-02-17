package com.talosvfx.talos.runtime.render.drawables;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticleDrawable;

public class ShaderDrawable implements ParticleDrawable {

    ShaderProgram shaderProgram;

    Texture texture;

    public ShaderDrawable() {
        shaderProgram = new ShaderProgram(getVert(), getFrag(""));
        shaderProgram.pedantic = false;

        texture = new Texture(Gdx.files.internal("fire.png"));
    }

    private String getFrag(String injectedCode) {
        String code = "";

        code += "#ifdef GL_ES\n";
        code += "#define LOWP lowp\n";
        code += "precision mediump float;\n";
        code += "#else\n";
        code += "#define LOWP\n";
        code += "#endif\n";
        code += "uniform sampler2D u_texture;\n";
        code += "varying LOWP vec4 v_color;\n";
        code += "varying vec2 v_texCoords;\n";
        code += "void main () {\n";
        code += "vec4 color = vec4(0.0);";
        code += injectedCode;
        code += "gl_FragColor = color;\n";
        code += "}\n";

        return code;
    }

    private String getVert() {
        String code = "";

        code += "attribute vec4 a_position; \n";
        code += "attribute vec4 a_color; \n";
        code += "attribute vec2 a_texCoord0; \n";
        code += "uniform mat4 u_projTrans; \n";
        code += "varying vec4 v_color; \n";
        code += "varying vec2 v_texCoords; \n";
        code += "void main () {\n";
        code += "v_color = a_color;\n";
        code += "v_texCoords = a_texCoord0;\n";
        code += "gl_Position = u_projTrans * a_position;\n";
        code += "}";

        return code;
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height, float rotation) {
        batch.setShader(shaderProgram);
        batch.draw(texture, x, y, width, height);
    }

    @Override
    public void draw(Batch batch, Particle particle, Color color) {
        float rotation = particle.rotation;
        float width = particle.size.x;
        float height = particle.size.y;
        if(Float.isInfinite(height)) height = width;
        float y = particle.getY();
        float x = particle.getX();
        draw(batch, x, y, width, height, rotation);
    }

    @Override
    public float getAspectRatio() {
        return 0;
    }

    @Override
    public void setSeed(float seed) {

    }

    @Override
    public TextureRegion getTextureRegion() {
        return null;
    }

    public void setCode(String code) {
        shaderProgram = new ShaderProgram(getVert(), getFrag(code));
        shaderProgram.pedantic = false;
    }
}
