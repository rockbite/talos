package com.talosvfx.talos.editor.addons.scene.apps.tween;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.apps.tween.nodes.TweenNode;
import com.talosvfx.talos.editor.widgets.ui.timeline.TimelineListener;
import com.talosvfx.talos.editor.widgets.ui.timeline.TimelineWidget;

public class AnimationTimeline extends TimelineWidget<TweenNode> {

    private Array<TrackRow> tracks = new Array<TrackRow>();

    public AnimationTimeline(Skin skin) {
        super(skin);

        getActionWidget().getNewButton().setVisible(false);

        addListener(new TimelineListener() {

        });
    }

    public void addTrack(TweenNode tweenNode) {
        TrackRow track = new TrackRow(tweenNode, tracks.size);
        tracks.add(track);
        setData(tracks);
    }

    @Override
    protected String getItemTypeName() {
        return "Track";
    }
}
