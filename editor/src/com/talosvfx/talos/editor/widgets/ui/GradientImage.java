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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;
import com.rockbite.bongo.engine.render.ShaderFlags;
import com.rockbite.bongo.engine.render.ShaderSourceProvider;
import com.rockbite.bongo.engine.render.SpriteShaderCompiler;
import com.talosvfx.talos.runtime.values.ColorPoint;

public class GradientImage extends Actor {

    ShaderProgram gradientShader;

    Skin skin;

    private Array<ColorPoint> points = new Array<>();

    Texture white;

    StringBuilder stringBuilder = new StringBuilder();

    private final String U_POINT_COUNT = "u_pointCount";
    private final String U_ARR_NAME = "u_gradientPoints[";

    private final String U_PARAM_COLOR = "].color";
    private final String U_PARAM_ALPHA = "].alpha";

    public GradientImage(Skin skin) {
        white = new Texture(Gdx.files.internal("white.png")); //TODO: not cool
        this.skin = skin;

        final FileHandle vertexSource = ShaderSourceProvider.resolveVertex("ui/gradient");
        final FileHandle fragmentSource = ShaderSourceProvider.resolveFragment("ui/gradient");

        gradientShader = SpriteShaderCompiler.getOrCreateShader("ui/gradient", vertexSource, fragmentSource, new ShaderFlags());

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

        batch.setShader(gradientShader);

        gradientShader.setUniformi(U_POINT_COUNT, points.size);

        for(int i = 0; i < points.size; i++){
            ColorPoint point = points.get(i);

            stringBuilder.clear();
            stringBuilder.append(U_ARR_NAME).append(i).append(U_PARAM_COLOR);
            gradientShader.setUniformf(stringBuilder.toString(), point.color.r, point.color.g, point.color.b, 1f);

            stringBuilder.clear();
            stringBuilder.append(U_ARR_NAME).append(i).append(U_PARAM_ALPHA);
            gradientShader.setUniformf(stringBuilder.toString(), point.pos);
        }

        // do the rendering
        batch.draw(white, getX(), getY(), getWidth(), getHeight());

        batch.setShader(prevShader);
    }

}
