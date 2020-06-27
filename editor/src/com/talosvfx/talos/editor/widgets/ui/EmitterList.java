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


        addListener(new TimelineListener() {
            @Override
            protected void onNewClicked() {
                createNewEmitterClicked();
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

    private void createNewEmitterClicked() {
        ParticleEmitterWrapper selectedItem = getSelectedItem();
        float sortPosition = 0;
        // if nothing is selected we are adding on top
        if(selectedItem != null) {
            sortPosition = selectedItem.getEmitter().getSortPosition() - 0.5f;
        }

        final ParticleEmitterWrapper emitter = TalosMain.Instance().TalosProject().createNewEmitter("emitter", sortPosition);
        Array<ParticleEmitterWrapper> activeWrappers = TalosMain.Instance().TalosProject().getActiveWrappers();
        addNewItem(emitter);

        // update all items
        updateItemData(activeWrappers);

        setSelected(emitter);
    }

    @Override
    protected String getItemTypeName() {
        return "Emitters";
    }

    public void setEmitters(Array<ParticleEmitterWrapper> emitterWrappers) {
        setData(emitterWrappers);
    }
}
