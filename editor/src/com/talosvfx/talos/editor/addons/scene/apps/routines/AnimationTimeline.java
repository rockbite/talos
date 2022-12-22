package com.talosvfx.talos.editor.addons.scene.apps.routines;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.apps.routines.nodes.RoutineNode;
import com.talosvfx.talos.editor.widgets.ui.timeline.TimelineListener;
import com.talosvfx.talos.editor.widgets.ui.timeline.TimelineWidget;

public class AnimationTimeline extends TimelineWidget<RoutineNode> {

    private Array<TrackRow> tracks = new Array<TrackRow>();

    public AnimationTimeline(Skin skin) {
        super(skin);

        getActionWidget().getNewButton().setVisible(false);

        addListener(new TimelineListener() {

        });
    }

    public void addTrack(RoutineNode routineNode) {
        TrackRow track = new TrackRow(routineNode, tracks.size);
        tracks.add(track);
        setData(tracks);
    }

    @Override
    protected String getItemTypeName() {
        return "Track";
    }
}
