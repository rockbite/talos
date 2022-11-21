package com.talosvfx.talos.editor.addons.scene.apps.tween;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.apps.tween.nodes.RoutineNode;
import com.talosvfx.talos.editor.widgets.ui.timeline.TimelineItemDataProvider;

class TrackRow implements TimelineItemDataProvider<RoutineNode> {

    private float duration = 10;
    private int index;

    private RoutineNode routineNode;

    public TrackRow(RoutineNode routineNode, int index) {
        this.routineNode = routineNode;
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
        return routineNode.getTweenTitle() + " track " + getIndex();
    }

    @Override
    public RoutineNode getIdentifier() {
        return routineNode;
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