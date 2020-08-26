package com.talosvfx.talos.editor.addons.bvb;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.ParticleEmitterWrapper;
import com.talosvfx.talos.editor.widgets.ui.timeline.TimelineListener;
import com.talosvfx.talos.editor.widgets.ui.timeline.TimelineWidget;

public class ParticleList extends TimelineWidget<BoundEffect> {

    BvBWorkspace workspace;

    public ParticleList (BvBWorkspace workspace, Skin skin) {
        super(skin);
        this.workspace = workspace;

        getActionWidget().getNewButton().setVisible(false);
        getActionWidget().getRepeatButton().setChecked(true);
        getActionWidget().getPlayButton().setChecked(!workspace.isPaused());

        addListener(new TimelineListener() {

            @Override
            protected void onPlayClicked () {
                workspace.setPaused(!getActionWidget().getPlayButton().isChecked());
            }

            @Override
            protected void onDeleteClicked () {
                Array<BoundEffect> selector = getSelector();
                BoundEffect selectedItem = getSelectedItem();
                if(selectedItem != null) {
                    if (!selector.contains(selectedItem, true)) {
                        selector.add(selectedItem);
                    }
                }

                for(BoundEffect effect: selector) {
                    if(effect != null) {
                        workspace.getSkeletonContainer().removeEffect(effect);
                        workspace.effectUnselected(effect);
                    }
                }

                removeItems(selector);
            }
        });
    }

    @Override
    protected String getItemTypeName () {
        return "Effects";
    }

    public void updateEffectList(Array<BoundEffect> list) {
        setData(list);
    }

    @Override
    public void act (float delta) {
        super.act(delta);
        if (workspace.getSkeletonContainer().getAnimationState() != null) {
            float animTime = workspace.getSkeletonContainer().getAnimationState().getTracks().first().getTrackTime();
            float duration = workspace.getSkeletonContainer().getCurrentAnimation().getDuration();

            float time = animTime % duration;

            setTimeCursor(time);
        }
    }

    public void setPaused (boolean paused) {
        getActionWidget().getPlayButton().setChecked(!paused);
    }
}
