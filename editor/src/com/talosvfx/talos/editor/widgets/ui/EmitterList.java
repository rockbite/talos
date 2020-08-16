package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.ParticleEmitterWrapper;
import com.talosvfx.talos.editor.widgets.ui.timeline.TimelineListener;
import com.talosvfx.talos.editor.widgets.ui.timeline.TimelineWidget;

public class EmitterList extends TimelineWidget<ParticleEmitterWrapper> {

    public EmitterList(Skin skin) {
        super(skin);

        getActionWidget().getRepeatButton().setChecked(true);
        getActionWidget().getPlayButton().setChecked(true);

        addListener(new TimelineListener() {
            @Override
            protected void onNewClicked() {
                createNewEmitterClicked();
            }

            @Override
            protected void onToggleLoop (boolean loopEnabled) {
                TalosMain.Instance().TalosProject().getParticleEffect().loopable = loopEnabled;
            }

            @Override
            protected void onUp () {
                moveItemSorting(1.5f);
            }

            @Override
            protected void onDown () {
                moveItemSorting(-1.5f);
            }

            @Override
            protected void onItemVisibilityChange (Object identifier, boolean isVisible) {
                ParticleEmitterWrapper wrapper = (ParticleEmitterWrapper) identifier;
                wrapper.isMuted = !isVisible;
                TalosMain.Instance().TalosProject().getParticleEffect().getEmitter(wrapper.getEmitter()).setVisible(isVisible);
            }

            @Override
            protected void onItemSelect(Object identifier) {
                ParticleEmitterWrapper selected = (ParticleEmitterWrapper) identifier;

                TalosMain.Instance().TalosProject().setCurrentEmitterWrapper(selected);
                TalosMain.Instance().NodeStage().moduleBoardWidget.setCurrentEmitter(selected);
            }

            @Override
            protected void onItemRename(Object identifier, String newName) {
                ParticleEmitterWrapper item = (ParticleEmitterWrapper) identifier;

                item.setName(newName);
            }

            @Override
            protected void onSkipToStartClicked () {
                TalosMain.Instance().TalosProject().getParticleEffect().restart();
                getActionWidget().getPlayButton().setChecked(true);
            }

            @Override
            protected void onPlayClicked () {
                boolean play = getActionWidget().getPlayButton().isChecked();

                if(play) {
                    TalosMain.Instance().TalosProject().getParticleEffect().resume();
                } else {
                    TalosMain.Instance().TalosProject().getParticleEffect().pause();
                }

            }

            @Override
            protected void onDeleteClicked() {
                Array<ParticleEmitterWrapper> selector = getSelector();
                ParticleEmitterWrapper selectedItem = getSelectedItem();
                if(selectedItem != null) {
                    if (!selector.contains(selectedItem, true)) {
                        selector.add(selectedItem);
                    }
                }

                for(ParticleEmitterWrapper wrapper: selector) {
                    TalosMain.Instance().TalosProject().removeEmitter(wrapper);
                }
                
                removeItems(selector);

                Array<ParticleEmitterWrapper> activeWrappers = TalosMain.Instance().TalosProject().getActiveWrappers();
                if(activeWrappers.size == 0) {
                    // we need to create default one
                    TalosMain.Instance().TalosProject().resetToNew();
                }
            }
        });
    }

    private void moveItemSorting(float moveBy) {
        ParticleEmitterWrapper selectedItem = getSelectedItem();

        if(selectedItem != null) {
            float sortPosition = selectedItem.getEmitter().getSortPosition() + moveBy;
            selectedItem.setPosition(sortPosition);
            TalosMain.Instance().TalosProject().sortEmitters();

            Array<ParticleEmitterWrapper> activeWrappers = TalosMain.Instance().TalosProject().getActiveWrappers();
            activeWrappers.reverse();
            setData(activeWrappers);
            setSelected(selectedItem);
        }
    }

    private void createNewEmitterClicked() {
        ParticleEmitterWrapper selectedItem = getSelectedItem();
        float sortPosition = 0;
        // if nothing is selected we are adding on top
        if(selectedItem != null) {
            sortPosition = selectedItem.getEmitter().getSortPosition() + 0.5f;
        }

        final ParticleEmitterWrapper emitter = TalosMain.Instance().TalosProject().createNewEmitter("emitter", sortPosition);
        Array<ParticleEmitterWrapper> activeWrappers = TalosMain.Instance().TalosProject().getActiveWrappers();

        // update all items
        activeWrappers.reverse();
        setData(activeWrappers);

        setSelected(emitter);
    }

    @Override
    protected String getItemTypeName() {
        return "Emitters";
    }

    public void setEmitters(Array<ParticleEmitterWrapper> emitterWrappers) {
        if(emitterWrappers.size > 0) {
            emitterWrappers.reverse();
            setData(emitterWrappers);
            setSelected(emitterWrappers.first());

            TalosMain.Instance().TalosProject().setCurrentEmitterWrapper(emitterWrappers.first());
            TalosMain.Instance().NodeStage().moduleBoardWidget.setCurrentEmitter(emitterWrappers.first());
        }
    }

    @Override
    public void act (float delta) {
        super.act(delta);

        float totalTime = TalosMain.Instance().TalosProject().getParticleEffect().getTotalTime();
        float duration = TalosMain.Instance().TalosProject().estimateTotalEffectDuration();

        float time = totalTime % duration;

        //setTimeCursor(time);
    }

    public void setPaused (boolean paused) {
        getActionWidget().getPlayButton().setChecked(paused);
    }
}
