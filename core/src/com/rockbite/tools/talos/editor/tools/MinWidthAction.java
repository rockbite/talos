package com.rockbite.tools.talos.editor.tools;

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;

public class MinWidthAction<T extends Cell> extends TemporalAction {

    private float targetMinWidth;
    private float startMinWidth;
    private float minWidth;

    private T iconCell;

    public void setTarget (float targetMinWidth) {
        this.targetMinWidth = targetMinWidth;
    }

    @Override
    protected void begin () {
        this.startMinWidth = iconCell.getMinWidth();
    }


    @Override
    protected void update (float percent) {
        minWidth = startMinWidth + (targetMinWidth - startMinWidth) * percent;
        iconCell.minWidth(minWidth);
        iconCell.getTable().invalidateHierarchy();
    }

    public void setTarget (T iconCell) {
        this.iconCell = iconCell;
    }
}