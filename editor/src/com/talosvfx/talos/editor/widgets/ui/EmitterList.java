package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.ParticleEmitterWrapper;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.project2.apps.ParticleNodeEditorApp;
import com.talosvfx.talos.editor.serialization.EmitterData;
import com.talosvfx.talos.editor.widgets.ui.timeline.BasicRow;
import com.talosvfx.talos.editor.widgets.ui.timeline.TimelineListener;
import com.talosvfx.talos.editor.widgets.ui.timeline.TimelineWidget;
import com.talosvfx.talos.runtime.vfx.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.vfx.ParticleEmitterDescriptor;

import java.util.Comparator;
import java.util.function.Supplier;

public class EmitterList extends TimelineWidget<ParticleEmitterWrapper> {

    private Preview3D preview;
    private ParticleNodeEditorApp editorApp;

    public EmitterList(Skin skin) {
        super(skin);

        /**
         * numbers represent render (low render first, high render last)
         * in the view, high number is on top, and low number is at bottom
         */

        setSortComparator(new Comparator<BasicRow<ParticleEmitterWrapper>>() {
            @Override
            public int compare(BasicRow<ParticleEmitterWrapper> o1, BasicRow<ParticleEmitterWrapper> o2) {
                return o2.getIndex() - o1.getIndex();
            }
        });

        getActionWidget().getRepeatButton().setChecked(true);
        getActionWidget().getPlayButton().setChecked(true);

        addListener(new TimelineListener() {
            @Override
            protected void onNewClicked() {
                createNewEmitterClicked();
            }

            @Override
            protected void onToggleLoop (boolean loopEnabled) {
                // TODO: 23.02.23 dummy refactor
                if (preview == null) {
                    return;
                }

                preview.getEffectInstance().loopable = loopEnabled;
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
                // TODO: 23.02.23 dummy refactor
                if (preview == null) {
                    return;
                }

                ParticleEmitterWrapper wrapper = (ParticleEmitterWrapper) identifier;
                wrapper.isMuted = !isVisible;
                preview.getEffectInstance().getEmitter(wrapper.getEmitter()).setVisible(isVisible);
            }

            @Override
            protected void onItemSelect(Object identifier) {
                ParticleEmitterWrapper selected = (ParticleEmitterWrapper) identifier;


                editorApp.getModuleBoardWidget().currentEmitterWrapper = selected;
                editorApp.getModuleBoardWidget().setCurrentEmitter(selected);
            }

            @Override
            protected void onItemRename(Object identifier, String newName) {
                ParticleEmitterWrapper item = (ParticleEmitterWrapper) identifier;

                item.setName(newName);
            }

            @Override
            protected void onSkipToStartClicked () {
                // TODO: 23.02.23 dummy refactor
                if (preview == null) {
                    return;
                }

                preview.getEffectInstance().restart();
                getActionWidget().getPlayButton().setChecked(true);
            }

            @Override
            protected void onPlayClicked () {

                // TODO: 23.02.23 dummy refactor
                if (preview == null) {
                    return;
                }

                boolean play = getActionWidget().getPlayButton().isChecked();

                if(play) {
                    preview.getEffectInstance().resume();
                } else {
                    preview.getEffectInstance().pause();
                }

            }

            @Override
            protected void onDeleteClicked() {
                // TODO: 23.02.23 dummy refactor
                if (preview == null) {
                    return;
                }

                Array<ParticleEmitterWrapper> selector = getSelector();
                ParticleEmitterWrapper selectedItem = getSelectedItem();
                if(selectedItem != null) {
                    if (!selector.contains(selectedItem, true)) {
                        selector.add(selectedItem);
                    }
                }

                for(ParticleEmitterWrapper wrapper: selector) {
                    removeEmitter(wrapper);
                }
                
                removeItems(selector);

                Array<ParticleEmitterWrapper> activeWrappers = editorApp.getEditorState().activeWrappers;

                if(activeWrappers.size == 0) {
                    // we need to create default one
                    editorApp.resetToNew();
                }
                AssetRepository.getInstance().assetChanged(editorApp.getGameAsset());
            }
        });
    }

    private void moveItemSorting(float moveBy) {
        ParticleEmitterWrapper selectedItem = getSelectedItem();

        if(selectedItem != null) {
            float sortPosition = selectedItem.getEmitter().getSortPosition() + moveBy;
            selectedItem.setPosition(sortPosition);
            editorApp.sortEmitters();
            Array<ParticleEmitterWrapper> activeWrappers = editorApp.getEditorState().activeWrappers;
            activeWrappers.reverse();
            setData(activeWrappers);
            setSelected(selectedItem);
        }
    }

    private void createNewEmitterClicked() {
        // TODO: 23.02.23 dummy refactor
        if (preview == null) {
            return;
        }

        ParticleEmitterWrapper selectedItem = getSelectedItem();
        float sortPosition = 0;
        // if nothing is selected we are adding on top
        if(selectedItem != null) {
            sortPosition = selectedItem.getEmitter().getSortPosition() + 0.5f;
        }

        ParticleEmitterWrapper emitter = createNewEmitter("emitter", sortPosition);
        Array<ParticleEmitterWrapper> activeWrappers = editorApp.getEditorState().activeWrappers;
        activeWrappers.reverse();
        setData(activeWrappers);
        setSelected(emitter);

        editorApp.getModuleBoardWidget().loadEmitterToBoard(emitter, new EmitterData());
    }

    @Override
    protected String getItemTypeName() {
        return "Emitters";
    }

    public void setEmitters(Array<ParticleEmitterWrapper> emitterWrappers) {
        if(emitterWrappers.size > 0) {
            setData(emitterWrappers);
            setSelected(emitterWrappers.first());
        }
    }

    @Override
    public void act (float delta) {
        super.act(delta);

        if(preview != null && preview.getEffectInstance() != null) {
            float totalTime = preview.getEffectInstance().getTotalTime();
            float duration = estimateTotalEffectDuration();
            float time = totalTime % duration;
            setTimeCursor(time);
        }
    }

    public void setPaused (boolean paused) {
        getActionWidget().getPlayButton().setChecked(paused);
    }

    public void setPreview(Preview3D preview3D) {
        this.preview = preview3D;
    }


    public float estimateTotalEffectDuration() {
        if(preview == null) return 0;

        Array<ParticleEmitterDescriptor> emitterDescriptors = preview.getDescriptor().emitterModuleGraphs;

        if (preview.getDescriptor().isContinuous()) {
            float maxWindow = 0;
            for(ParticleEmitterDescriptor emitter: emitterDescriptors) {
                if(emitter.getEmitterModule() != null) {
                    float duration = emitter.getEmitterModule().getDuration();

                    float totalWaitTime = duration;

                    if (maxWindow < totalWaitTime) {
                        maxWindow = totalWaitTime;
                    }
                }
            }
            return maxWindow;
        } else {
            float furthestPoint = 0;
            for(ParticleEmitterDescriptor emitter: emitterDescriptors) {
                if(emitter.getEmitterModule() != null && emitter.getParticleModule() != null) {
                    float delay = emitter.getEmitterModule().getDelay();
                    float duration = emitter.getEmitterModule().getDuration();
                    float life = emitter.getParticleModule().getLife();

                    float point = delay + duration + life;

                    if (furthestPoint < point) {
                        furthestPoint = point;
                    }
                }
            }

            return furthestPoint;
        }
    }

    private ParticleEmitterWrapper createNewEmitter (String emitterName, float sortPosition) {
        ParticleEmitterWrapper emitterWrapper = editorApp.initEmitter(emitterName);
        Supplier<ParticleEffectDescriptor> descriptorSupplier = editorApp.getGameAsset().getResource().getDescriptorSupplier();
        ParticleEffectDescriptor particleEffectDescriptor = descriptorSupplier.get();
        particleEffectDescriptor.addEmitter(emitterWrapper.getEmitter());

        editorApp.getEditorState().activeWrappers.add(emitterWrapper);

        editorApp.getModuleBoardWidget().currentEmitterWrapper = emitterWrapper;

        emitterWrapper.setPosition(sortPosition);
        editorApp.sortEmitters();

        //todo: mark as modified to save

        editorApp.getModuleBoardWidget().setCurrentEmitter(emitterWrapper);

        AssetRepository.getInstance().assetChanged(editorApp.getGameAsset());

        return emitterWrapper;
    }

    public void setEditorApp(ParticleNodeEditorApp app) {
        this.editorApp = app;
    }

    public void removeEmitter (ParticleEmitterWrapper wrapper) {
        // TODO: 23.02.23 dummy refactor
        if (preview == null) {
            return;
        }

        editorApp.getModuleBoardWidget().removeEmitter(wrapper);
        preview.getEffectInstance().removeEmitterForEmitterDescriptor(wrapper.getEmitter());
        preview.getDescriptor().removeEmitter(wrapper.getEmitter());
        Array<ParticleEmitterWrapper> activeWrappers = editorApp.getEditorState().activeWrappers;

        activeWrappers.removeValue(wrapper, true);
        if (activeWrappers.size  <= 0) {
            createNewEmitterClicked();
        }
        editorApp.getModuleBoardWidget().setCurrentEmitter(activeWrappers.peek());

        //todo : node stage onEmitterRemoved call needed?

        setEmitters(activeWrappers);
    }
}
