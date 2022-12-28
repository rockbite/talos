package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.misc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class ProgressWidget extends Table {

    private final TextureAtlas.AtlasRegion region;
    private final Image progressImage;

    private ObjectFloatMap<String> progressMap = new ObjectFloatMap<>();

    private ShaderProgram shaderProgram;

    public ProgressWidget() {
        super(SharedResources.skin);

        region = getSkin().getAtlas().findRegion("mini-node-bg");

        progressImage = new Image(ColorLibrary.obtainBackground(getSkin(), "mini-node-bg", ColorLibrary.BackgroundColor.GREEN));
        progressImage.setSize(110, 110);
        progressImage.setPosition(-progressImage.getWidth()/2, -progressImage.getHeight()/2);
        progressImage.setRotation(180);
        progressImage.setOrigin(Align.center);
        addActor(progressImage);

        shaderProgram = new ShaderProgram(Gdx.files.internal("addons/scene/shaders/default.vert.glsl"), Gdx.files.internal("addons/scene/shaders/circularbar.frag.glsl"));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {

        ShaderProgram prevShader = null;

        if(progressMap.size > 0) {
            // enable shader
            prevShader = batch.getShader();

            batch.setShader(shaderProgram);
            shaderProgram.setUniformf("regionU", region.getU());
            shaderProgram.setUniformf("regionV", region.getV());
            shaderProgram.setUniformf("regionU2", region.getU2());
            shaderProgram.setUniformf("regionV2", region.getV2());

            int i = 0;
            for(ObjectFloatMap.Entry<String> entry : progressMap) {
                float progress = entry.value;
                shaderProgram.setUniformf("alpha[" + (i++) + "]", progress);
            }
            shaderProgram.setUniformi("alphaCount", progressMap.size);
        }

        super.draw(batch, parentAlpha);

        if(prevShader != null) {
            // change shader back
            batch.setShader(prevShader);
        }
    }

    public void setProgress(ObjectFloatMap<String> progressMap) {
        this.progressMap = progressMap;

        float min = 1;
        for(ObjectFloatMap.Entry<String> entry : progressMap) {
            float progress = entry.value;
            if(min > progress) min = progress;
        }

        progressImage.setRotation(180 - min * 90);
    }
}
