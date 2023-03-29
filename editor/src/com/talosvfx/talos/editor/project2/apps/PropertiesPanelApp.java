package com.talosvfx.talos.editor.project2.apps;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.talosvfx.talos.editor.addons.scene.logic.PropertyWrapperProviders;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectNameChanged;
import com.talosvfx.talos.editor.addons.scene.events.PropertyHolderSelected;
import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.widgets.SEPropertyPanel;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;

@SingletonApp
public class PropertiesPanelApp extends AppManager.BaseApp<Object> implements Observer {

	private final SEPropertyPanel propertyPanel;

	public PropertiesPanelApp () {
		this.singleton = true;

		propertyPanel = new SEPropertyPanel();
		DummyLayoutApp<Object> propertyPanelApp = new DummyLayoutApp<Object>(SharedResources.skin, this, getAppName()) {
			@Override
			public Actor getMainContent () {
				return propertyPanel;
			}

			@Override
			public void onInputProcessorAdded () {
				super.onInputProcessorAdded();
				SharedResources.stage.setScrollFocus(propertyPanel.getScrollPane());
			}

			@Override
			public void onInputProcessorRemoved () {
				super.onInputProcessorRemoved();
			}

			@Override
			public boolean hasPreferredWidth () {
				return true;
			}
		};

		this.gridAppReference = propertyPanelApp;
		Notifications.registerObserver(this);
	}

	@Override
	public void updateForGameAsset (GameAsset<Object> gameAsset) {
		super.updateForGameAsset(gameAsset);
		if (gameAsset.getResource() != null) {
			IPropertyHolder propertyHolderOrShouldBe = PropertyWrapperProviders.getOrCreateHolder(gameAsset.getResource());
			propertyPanel.showPanel(propertyHolderOrShouldBe, propertyHolderOrShouldBe.getPropertyProviders());
		}
	}

	@Override
	public String getAppName () {
		if (gameAsset != null) {
			return "Properties - " + gameAsset.nameIdentifier;
		} else {
			return "Properties - ";
		}
	}

	@Override
	public void onRemove () {

	}

	@EventHandler
	public void onGameObjectNameChanged(GameObjectNameChanged event) {
		getGridAppReference().updateTabName("properties - " + event.target.getName());
	}


	@EventHandler
	public void onPropertyHolderSelected(PropertyHolderSelected event) {
		getGridAppReference().updateTabName("Properties - " + event.getTarget().getName());
	}
}

