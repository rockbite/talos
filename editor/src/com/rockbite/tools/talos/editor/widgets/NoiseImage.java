/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.rockbite.tools.talos.editor.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.rockbite.tools.talos.runtime.utils.SimplexNoise;

public class NoiseImage extends Actor {

    private Skin skin;
    Texture white;
    ShaderProgram shaderProgram;

    private float frequency = 20f;

    Pixmap pixmap;

    public NoiseImage(Skin skin) {
        this.skin = skin;
        white = new Texture(Gdx.files.internal("white.png")); //TODO: not cool
        shaderProgram = new ShaderProgram(Gdx.files.internal("shaders/ui/default.vert"), Gdx.files.internal("shaders/ui/noise.frag"));

        pixmap = new Pixmap(165, 100, Pixmap.Format.RGB888);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(Gdx.input.isKeyPressed(Input.Keys.D)) {
            //drawPixmap(batch, parentAlpha);
        } else {
            drawWithShader(batch, parentAlpha);
        }
    }

    public void drawWithShader(Batch batch, float parentAlpha) {
        ShaderProgram prevShader = batch.getShader();
        batch.setShader(shaderProgram);

        shaderProgram.setUniformf("frequency", frequency);

        // do the rendering
        batch.draw(white, getX(), getY(), getWidth(), getHeight());

        batch.setShader(prevShader);
    }

    /**
     * @deprecated
     *
     * WARNING THIS IS FOR TESTING PURPOSES ONLY,
     * DO NOT USE AS THIS HAS HORRIBLE PERFORMANCE
     *
     * @param batch
     * @param parentAlpha
     */
    public void drawPixmap(Batch batch, float parentAlpha) {
        SimplexNoise simplexNoise = new SimplexNoise();
        pixmap.setColor(0, 0, 0, 1f);
        pixmap.fill();
        for(int x = 0; x < 165; x++) {
            for(int y = 0; y < 100; y++) {
                float v = simplexNoise.query(x/165f, y/100f, frequency) *0.5f + 0.5f;
                pixmap.setColor(v, v, v, 1f);
                pixmap.drawPixel(x, y);
            }
        }

        Texture texture = new Texture(pixmap, Pixmap.Format.RGB888, false);
        batch.draw(texture, getX(), getY());
    }

    public void setFrequency(float value) {
        frequency = value;
    }
}
