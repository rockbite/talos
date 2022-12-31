package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.misc;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.AsyncRoutineNodeWidget;

public class InterpolationTimeline extends Table {
    private final MicroNodeView microNodeView;
    private final AsyncRoutineNodeWidget asyncRoutineNodeWidget;
    private Group container = new Group();
    private ObjectMap<Object, Image> progressMap = new ObjectMap<>();
    private Interpolation interpolation = Interpolation.linear;

    private Image chartImage;

    public InterpolationTimeline(AsyncRoutineNodeWidget asyncRoutineNodeWidget, Skin skin) {
        super(skin);

        this.asyncRoutineNodeWidget = asyncRoutineNodeWidget;
        this.microNodeView = asyncRoutineNodeWidget.getMicroNodeView();

        setBackground(getSkin().getDrawable("timelinebg"));

        chartImage = new Image();
        chartImage.setFillParent(true);

        addActor(chartImage);
        addActor(container);
    }

    public void clearMap() {
        for (ObjectMap.Entry<Object, Image> objectImageEntry : progressMap) {
            objectImageEntry.value.remove();
            Pools.free(objectImageEntry.value);

        }
        progressMap.clear();
    }

    public void setProgress(Object target, float value) {
        if(!progressMap.containsKey(target)) {
            Image image = Pools.obtain(Image.class);
            image.setDrawable(getSkin().getDrawable("white"));
            image.setColor(Color.valueOf("#37574a"));
            progressMap.put(target, image);
            container.addActor(image);
        }

        Image image = progressMap.get(target);

        image.getColor().a = 0.4f;

        float alpha = value;

        image.setPosition(1, 1);

        if(alpha * (getWidth() - 1) > 0) {
            float width = alpha * (getWidth() - 1);
            if(width > getWidth() - 2) width = getWidth() - 2;
            image.setSize(width, getHeight() - 2);
            image.setVisible(true);
        } else {
            image.setVisible(false);
        }

        if(alpha == 0 || alpha == 1) {
            image.setVisible(false);
        } else {
            image.setVisible(true);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public float getPrefHeight() {
        return 58;
    }

    public void setInterpolation(Interpolation interpolation) {
        this.interpolation = interpolation;

        buildChart();
    }

    private void buildChart() {
        float width = 200;
        float height = 58;
        int res = 50;
        Pixmap pixmap = new Pixmap((int)width, 58, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.valueOf("18db66ff"));

        float margin = 0.2f;

        Vector2 prev = new Vector2(0, height);
        float result = interpolation.apply(0);
        prev.set(0, (int) (height - (result*height*(1f-margin))- height * margin/2f));
        for(int i = 1; i < res; i++) {
            int posX = (int) (i * (width/res));
            float a = (float)i/res;
            result = interpolation.apply(a);
            pixmap.drawLine((int) prev.x, (int) prev.y, posX, (int) (height - (result*height*(1f-margin))- height * margin/2f));
            prev.set(posX, (int) (height - (result*height*(1f-margin))- height * margin/2f));
        }

        Texture texture = new Texture(pixmap);
        TextureRegion region = new TextureRegion(texture);
        chartImage.setDrawable(new TextureRegionDrawable(region));
    }
}
