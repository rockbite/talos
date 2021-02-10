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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.FloatCounter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.PerformanceCounter;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.TimeUtils;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.wrappers.IDragPointProvider;
import com.talosvfx.talos.runtime.ParticleEffectInstance;
import com.talosvfx.talos.runtime.render.ParticleRenderer;
import com.talosvfx.talos.runtime.render.SpriteBatchParticleRenderer;

public abstract class PreviewWidget extends ViewportWidget {

    Vector2 mid = new Vector2();

    Vector2 temp = new Vector2();

    private String countStr = "Particles: ";
    private String trisCountStr = "Triangles: ";
    private String nodeCallsStr = "Node Calls: ";
    private String gpuTimeStr = "GPU: ";
    private String cpuTimeStr = "CPU: ";
    private String msStr = "ms";

    private Label countLbl;
    private Label trisCountLbl;
    private Label nodeCallsLbl;
    private Label gpuTimeLbl;
    private Label cpuTimeLbl;

    protected GLProfiler glProfiler = new GLProfiler(Gdx.graphics);
    protected FPSLogger fpsLogger = new FPSLogger();
    protected PerformanceCounter performanceCounter = new PerformanceCounter("talos");

    protected StringBuilder stringBuilder = new StringBuilder();
    protected int trisCount = 0;
    protected FloatCounter renderTime = new FloatCounter(100);
    protected FloatCounter cpuTime = new FloatCounter(100);
    protected float fps = 0;

    public PreviewWidget() {
        super();

        countLbl = new Label(countStr, TalosMain.Instance().getSkin());
        trisCountLbl = new Label(trisCountStr, TalosMain.Instance().getSkin());
        nodeCallsLbl = new Label(nodeCallsStr, TalosMain.Instance().getSkin());
        gpuTimeLbl = new Label(gpuTimeStr, TalosMain.Instance().getSkin());
        cpuTimeLbl = new Label(cpuTimeStr, TalosMain.Instance().getSkin());

        countLbl.setColor(Color.GRAY);
        trisCountLbl.setColor(Color.GRAY);
        nodeCallsLbl.setColor(Color.GRAY);
        gpuTimeLbl.setColor(Color.GRAY);
        cpuTimeLbl.setColor(Color.GRAY);

        add(countLbl).left().top().padLeft(5).row();
        add(trisCountLbl).left().top().padLeft(5).row();
        add(nodeCallsLbl).left().top().padLeft(5).row();
        add(cpuTimeLbl).left().top().padLeft(5).row();
        add(gpuTimeLbl).left().top().padLeft(5).row();
        add().expand();
        row();
        add(buildPreviewController()).bottom().left().growX();
    }

    protected abstract Table buildPreviewController();

    public abstract void resetToDefaults();

    @Override
    public void act(float delta) {
        super.act(delta);


        long timeBefore = TimeUtils.nanoTime();
        final ParticleEffectInstance particleEffect = TalosMain.Instance().TalosProject().getParticleEffect();
        particleEffect.update(Gdx.graphics.getDeltaTime());
        cpuTime.put( TimeUtils.timeSinceNanos(timeBefore));

        stringBuilder.clear();
        stringBuilder.append(countStr).append(particleEffect.getParticleCount());
        countLbl.setText(stringBuilder.toString());

        stringBuilder.clear();
        stringBuilder.append(trisCountStr).append(trisCount);
        trisCountLbl.setText(stringBuilder.toString());

        stringBuilder.clear();
        stringBuilder.append(nodeCallsStr).append(particleEffect.getNodeCalls());
        nodeCallsLbl.setText(stringBuilder.toString());

        float rt = renderTime.value/1000000f;
        float cp = cpuTime.value/1000000f;

        rt = (float)Math.round(rt * 10000f) / 10000f;
        cp = (float)Math.round(cp * 10000f) / 10000f;

        stringBuilder.clear();
        stringBuilder.append(gpuTimeStr).append(rt).append(msStr);
        gpuTimeLbl.setText(stringBuilder.toString());

        stringBuilder.clear();
        stringBuilder.append(cpuTimeStr).append(cp).append(msStr);
        cpuTimeLbl.setText(stringBuilder.toString());
    }


    @Override
    public void drawContent(Batch batch, float parentAlpha) {


    }

    public GLProfiler getGLProfiler() {
        return glProfiler;
    }

    public abstract void fileDrop(float x, float y, String[] paths);
    public abstract void unregisterDragPoints();
    public abstract void registerForDragPoints(IDragPointProvider dragPointProvider);
    public abstract void unregisterDragPoints(IDragPointProvider dragPointProvider);

    public abstract String getBackgroundImagePath();
    public abstract boolean isGridVisible();
    public abstract boolean isBackgroundImageInBack();
    public abstract float getBgImageSize();
    public abstract float getGridSize();

    public abstract void setBackgroundImage(String bgImagePath);
    public abstract void setGridVisible(boolean isGridVisible);
    public abstract void setImageIsBackground(boolean bgImageIsInBack);
    public abstract void setBgImageSize(float bgImageSize);
    public abstract void setGridSize(float gridSize);
}
