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
import com.talosvfx.talos.editor.addons.shader.ShaderBuilder;

public class ShaderBox extends Actor {

    ShaderProgram shaderProgram;
    ShaderBuilder shaderBuilder;

    Skin skin;

    Texture white;

    public ShaderBox() {
        white = new Texture(Gdx.files.internal("white.png")); //TODO: not cool
        this.skin = skin;
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
            }

            batch.draw(white, getX(), getY(), getWidth(), getHeight());
        } else {
            Color prevColor = batch.getColor();
            batch.setColor(Color.BLACK);
            batch.draw(white, getX(), getY(), getWidth(), getHeight());
            batch.setColor(prevColor);
        }

        batch.setShader(prevShader);
    }
}
