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

package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.values.ColorPoint;

public class GradientImage extends Actor {

    ShaderProgram shaderProgram;

    Skin skin;

    private Array<ColorPoint> points = new Array<>();

    Texture white;

    public GradientImage(Skin skin) {
        white = new Texture(Gdx.files.internal("white.png")); //TODO: not cool
        this.skin = skin;
        shaderProgram = new ShaderProgram(Gdx.files.internal("shaders/ui/gradient.vert"), Gdx.files.internal("shaders/ui/gradient.frag"));
        //System.out.println(shaderProgram.getLog());
    }

    public void setPoints(Array<ColorPoint> points) {
        this.points.clear();

        if(points.get(0).pos > 0) {
            this.points.add(new ColorPoint(points.get(0).color, 0f));
        }

        for(int i = 0; i < points.size; i++) {
            ColorPoint point = points.get(i);
            ColorPoint newPoint = new ColorPoint();
            newPoint.set(point);
            this.points.add(newPoint);
        }

        if(points.get(points.size-1).pos < 1) {
            this.points.add(new ColorPoint(points.get(points.size-1).color, 1f));
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        ShaderProgram prevShader = batch.getShader();
        batch.setShader(shaderProgram);

        shaderProgram.setUniformi("u_pointCount", points.size);

        for(int i = 0; i < points.size; i++){
            ColorPoint point = points.get(i);
            shaderProgram.setUniformf("u_gradientPoints[" + i + "].color", point.color.r, point.color.g, point.color.b, 1f);
            shaderProgram.setUniformf("u_gradientPoints[" + i + "].alpha", point.pos);
        }

        // do the rendering
        batch.draw(white, getX(), getY(), getWidth(), getHeight());

        batch.setShader(prevShader);
    }
}
