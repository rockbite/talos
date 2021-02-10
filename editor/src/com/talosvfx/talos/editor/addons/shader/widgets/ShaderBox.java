package com.talosvfx.talos.editor.addons.shader.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.runtime.shaders.ShaderBuilder;

public class ShaderBox extends Actor {

    ShaderProgram shaderProgram;
    ShaderBuilder shaderBuilder;

    Skin skin;

    Texture white;

    private Blending blending = Blending.NORMAL;

    public ShaderBox() {
        white = new Texture(Gdx.files.internal("white.png")); //TODO: not cool
        this.skin = skin;
    }

    public void setBlending (Blending blending) {
        this.blending = blending;
    }

    public enum Blending {
        NORMAL,
        ADDITIVE,
        BLENDADD
    }

    public void setShader(ShaderBuilder shaderBuilder) {
        this.shaderBuilder = shaderBuilder;
        ShaderProgram shaderProgram = shaderBuilder.getShaderProgram();

        if(shaderProgram.isCompiled()) {
            this.shaderProgram = shaderProgram;
        } else {
            this.shaderProgram = null;
            System.out.println(shaderProgram.getLog());
            System.out.println(shaderProgram.getFragmentShaderSource());
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        ShaderProgram prevShader = batch.getShader();

        if (blending == Blending.NORMAL) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        if (blending == Blending.ADDITIVE) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        }
        if (blending == Blending.BLENDADD) {
            batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }

        if(shaderProgram != null && shaderProgram.isCompiled()) {
            batch.setShader(shaderProgram);

            /**
             * Set uniforms and other stuff after this line
             */

            ObjectMap<String, ShaderBuilder.UniformData> declaredUniforms = shaderBuilder.getDeclaredUniforms();
            int bind = 1;
            for(ShaderBuilder.UniformData data: declaredUniforms.values()) {
                if(data.type == ShaderBuilder.Type.TEXTURE && data.payload != null) {
                    Texture texture = (Texture) data.payload.getValue();
                    texture.bind(bind);
                    shaderProgram.setUniformi(data.variableName, bind);
                    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

                    bind++;
                }
                if(data.type == ShaderBuilder.Type.FLOAT && data.payload instanceof ShaderBuilder.IValueProvider) {
                    ShaderBuilder.IValueProvider provider = data.payload;
                    shaderProgram.setUniformf(data.variableName, (float)provider.getValue());
                }
                if(data.type == ShaderBuilder.Type.VEC4 && data.payload instanceof ShaderBuilder.IValueProvider) {
                    ShaderBuilder.IValueProvider provider = data.payload;
                    shaderProgram.setUniformf(data.variableName, (Color) provider.getValue());
                }
            }

            batch.draw(white, getX(), getY(), getWidth(), getHeight());
        } else {
            Color prevColor = batch.getColor();
            batch.setColor(Color.BLACK);
            batch.draw(white, getX(), getY(), getWidth(), getHeight());
            batch.setColor(prevColor);
        }

        batch.setShader(prevShader);

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
