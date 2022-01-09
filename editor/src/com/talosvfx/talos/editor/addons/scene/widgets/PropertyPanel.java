package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.bvb.PropertiesPanel;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectNameChanged;
import com.talosvfx.talos.editor.addons.scene.events.PropertyHolderSelected;
import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;

import java.util.Comparator;

public class PropertyPanel extends Table implements Notifications.Observer {

    Table container;
    Table fakeContainer;
    ScrollPane scrollPane;

    private ObjectMap<Class, IPropertyProvider> providerSet = new ObjectMap<>();
    private Array<PropertiesPanel> panelList = new Array<>();
    private ObjectMap<IPropertyProvider, PropertiesPanel> providerPanelMap = new ObjectMap<>();
    private IPropertyHolder currentPropertyHolder;

    public PropertyPanel() {
        setSkin(TalosMain.Instance().getSkin());

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

    public void showPanel (IPropertyHolder target, Iterable<IPropertyProvider> propertyProviders) {
        providerSet.clear();
        for(IPropertyProvider propertyProvider: propertyProviders) {
            if(propertyProvider.getType() == null) continue;
            providerSet.put(propertyProvider.getType(), propertyProvider);
        }
        build();
        currentPropertyHolder = target;
    }

    public void build() {
        container.clear();

        Array<IPropertyProvider> list = new Array<>();

        for(IPropertyProvider provider: providerSet.values()) {
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
            PropertiesPanel panel = new PropertiesPanel(provider, getSkin());

            container.add(panel).growX().top().padBottom(5);
            container.row();

            panelList.add(panel);

            providerPanelMap.put(provider, panel);
        }
    }

    public void hidePanel(IPropertyProvider propertyProvider) {
        if(propertyProvider == null) return;
        providerSet.remove(propertyProvider.getClass());
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

    public void propertyProviderUpdated (IPropertyProvider propertyProvider) {
        if(providerPanelMap.containsKey(propertyProvider)) {
            providerPanelMap.get(propertyProvider).updateValues();
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
