package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;

public class HeightAction<T extends Cell> extends TemporalAction {
    private float targetHeight;
    private float startHeight;
    private float height;
    private T widgetCell;
    public void setTarget (float targetHeight) {
        this.targetHeight = targetHeight;
    }
    @Override
    public void begin () {
        startHeight = widgetCell.getPrefHeight();
    }
    @Override
    protected void update (float percent) {
        height = startHeight + (targetHeight - startHeight) * percent;
        widgetCell.height(height);
        widgetCell.getTable().invalidateHierarchy();
    }
    public void setTarget (T widgetCell) {
        this.widgetCell = widgetCell;
    }
}