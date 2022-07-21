package com.talosvfx.talos.editor.addons.scene.apps.tween;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.apps.tween.nodes.TweenNode;
import com.talosvfx.talos.editor.widgets.ui.timeline.TimelineItemDataProvider;

class TrackRow implements TimelineItemDataProvider<TweenNode> {

    private float duration = 10;
    private int index;

    private TweenNode tweenNode;

    public TrackRow(TweenNode tweenNode, int index) {
        this.tweenNode = tweenNode;
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
        return tweenNode.getTweenTitle() + " track " + getIndex();
    }

    @Override
    public TweenNode getIdentifier() {
        return tweenNode;
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