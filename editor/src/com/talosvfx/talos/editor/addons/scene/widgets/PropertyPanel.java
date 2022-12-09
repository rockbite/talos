package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.bvb.PropertiesPanel;
import com.talosvfx.talos.editor.addons.scene.events.ComponentAdded;
import com.talosvfx.talos.editor.addons.scene.events.ComponentUpdated;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectNameChanged;
import com.talosvfx.talos.editor.addons.scene.events.PropertyHolderSelected;
import com.talosvfx.talos.editor.addons.scene.events.meta.MetaDataReloadedEvent;
import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.logic.components.ScriptComponent;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;

public class PropertyPanel extends Table implements Observer {

    @Setter
    boolean ignoringEvents;

    Table container;
    Table fakeContainer;

    @Getter
    ScrollPane scrollPane;

    private Array<IPropertyProvider> providerSet = new Array<>();
    private Array<PropertiesPanel> panelList = new Array<>();
    private ObjectMap<IPropertyProvider, PropertiesPanel> providerPanelMap = new ObjectMap<>();
    private IPropertyHolder currentPropertyHolder;

    public PropertyPanel() {
        setSkin(SharedResources.skin);

        container = new Table();
        fakeContainer = new Table();
        scrollPane = new ScrollPane(fakeContainer);
        scrollPane.setSmoothScrolling(false);
        scrollPane.setOverscroll(false, false);
        add(scrollPane).grow();

        fakeContainer.add(container).growX().row();
        fakeContainer.add().expandY();

        Notifications.registerObserver(this);
    }

    @EventHandler
    public void onGameObjectNameChanged(GameObjectNameChanged event) {
        PropertiesPanel propertiesPanel = providerPanelMap.get(event.target);
        if(propertiesPanel != null) {
            propertiesPanel.updateValues();
        }
    }


    @EventHandler
    public void onPropertyHolderSelected(PropertyHolderSelected event) {
        if(event.getTarget() == null) return;
        if(event.getTarget().getPropertyProviders() == null) return;

        showPanel(event.getTarget(), event.getTarget().getPropertyProviders());
    }

//    @EventHandler
//    public void onPropertyHolderEdited (PropertyHolderEdited event) {
//        Object parentOfPropertyHolder = event.parentOfPropertyHolder;
//
//        if (!event.fastChange) {
//            if (parentOfPropertyHolder instanceof AMetadata) {
//                AssetRepository.getInstance().saveMetaData((AMetadata)parentOfPropertyHolder, true);
//            }
//        }
//
//    }

    @EventHandler
    public void onMetaDataReloadedEvent (MetaDataReloadedEvent event) {
        if (currentPropertyHolder == event.getMetadata()) {
            updateValues();
        }
    }

    public void showPanel (IPropertyHolder target, Iterable<IPropertyProvider> propertyProviders) {
        providerSet.clear();
        for(IPropertyProvider propertyProvider: propertyProviders) {
            if(propertyProvider.getType() == null) continue;
            providerSet.add(propertyProvider);
        }
        build();
        currentPropertyHolder = target;
    }

    public void build() {
        container.clear();

        Array<IPropertyProvider> list = new Array<>();

        for (IPropertyProvider provider : providerSet) {
            list.add(provider);
        }

        list.sort(new Comparator<IPropertyProvider>() {
            @Override
            public int compare(IPropertyProvider o1, IPropertyProvider o2) {
                return o1.getPriority()-o2.getPriority();
            }
        });

        panelList.clear();
        providerPanelMap.clear();

        for(IPropertyProvider provider: list) {
            PropertiesPanel panel = new PropertiesPanel(provider, getSkin(), this);

            container.add(panel).growX().top().padBottom(5);
            container.row();

            panelList.add(panel);

            providerPanelMap.put(provider, panel);
        }
    }

    public void hidePanel(IPropertyProvider propertyProvider) {
        if(propertyProvider == null) return;
        providerSet.removeValue(propertyProvider, true);
        build();

        currentPropertyHolder = null;
    }

    public void updateValues() {
        for(PropertiesPanel panel: panelList) {
            panel.updateValues();
        }
    }

    public void cleanPanels() {
        container.clear();
        providerSet.clear();
        panelList.clear();
        currentPropertyHolder = null;
    }

    @EventHandler
    public void onComponentAdded (ComponentAdded componentAdded) {
        if (this.currentPropertyHolder == componentAdded.getParent()) {
            showPanel(this.currentPropertyHolder, this.currentPropertyHolder.getPropertyProviders());
        }
    }
    @EventHandler
    public void onComponentUpdate (ComponentUpdated componentUpdated) {
        if (!ignoringEvents) {
            propertyProviderUpdated(componentUpdated.getComponent());
        }
    }

    public void propertyProviderUpdated (IPropertyProvider propertyProvider) {
        if(providerPanelMap.containsKey(propertyProvider)) {
            PropertiesPanel propertiesPanel = providerPanelMap.get(propertyProvider);
            if (propertyProvider instanceof ScriptComponent) {
                propertiesPanel.reconstruct();
            } else {
                propertiesPanel.updateValues();
            }
        }
    }

    public void notifyPropertyHolderRemoved (IPropertyHolder propertyHolder) {
        if(currentPropertyHolder == propertyHolder) {
            cleanPanels();
        }
    }

    public IPropertyHolder getCurrentHolder () {
        return currentPropertyHolder;
    }

}
