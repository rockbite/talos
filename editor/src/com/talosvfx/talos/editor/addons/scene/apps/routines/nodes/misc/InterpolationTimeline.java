package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.misc;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.AsyncRoutineNodeWidget;

public class InterpolationTimeline extends Table {
    private final MicroNodeView microNodeView;
    private final AsyncRoutineNodeWidget asyncRoutineNodeWidget;
    private Group container = new Group();
    private ObjectMap<Object, Image> progressMap = new ObjectMap<>();

    public InterpolationTimeline(AsyncRoutineNodeWidget asyncRoutineNodeWidget, Skin skin) {
        super(skin);

        this.asyncRoutineNodeWidget = asyncRoutineNodeWidget;
        this.microNodeView = asyncRoutineNodeWidget.getMicroNodeView();

        setBackground(getSkin().getDrawable("timelinebg"));

        addActor(container);
    }


    public void setProgress(Object target, float value) {
        if(!progressMap.containsKey(target)) {
            Image image = new Image(getSkin().getDrawable("white"));
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
}
