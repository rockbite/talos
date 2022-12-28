package com.talosvfx.talos.editor.addons.scene.apps.routines;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.RoutineExecuteNodeWidget;
import com.talosvfx.talos.editor.widgets.ui.timeline.TimelineItemDataProvider;

class TrackRow implements TimelineItemDataProvider<RoutineExecuteNodeWidget> {

    private float duration = 10;
    private int index;

    private RoutineExecuteNodeWidget routineExecuteNodeWidget;

    public TrackRow(RoutineExecuteNodeWidget routineExecuteNodeWidget, int index) {
        this.routineExecuteNodeWidget = routineExecuteNodeWidget;
        this.index = index;
    }

    @Override
    public Array<Button> registerSecondaryActionButtons() {
        return null;
    }

    @Override
    public Array<Button> registerMainActionButtons() {
        return null;
    }

    @Override
    public String getItemName() {
        return routineExecuteNodeWidget.getTweenTitle() + " track " + getIndex();
    }

    @Override
    public RoutineExecuteNodeWidget getIdentifier() {
        return routineExecuteNodeWidget;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public boolean isFull() {
        return true;
    }

    @Override
    public float getDurationOne() {
        return duration;
    }

    @Override
    public float getDurationTwo() {
        return duration;
    }

    @Override
    public float getTimePosition() {
        return 0;
    }

    @Override
    public boolean isItemVisible() {
        return true;
    }

    @Override
    public void setTimePosition(float time) {

    }
}