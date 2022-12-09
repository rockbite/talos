package com.talosvfx.talos.editor.project2.apps;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.widgets.SEPropertyPanel;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;

@SingletonApp
public class PropertiesPanelApp extends AppManager.BaseApp<IPropertyHolder> {

	private final SEPropertyPanel propertyPanel;

	public PropertiesPanelApp () {
		this.singleton = true;

		propertyPanel = new SEPropertyPanel();
		DummyLayoutApp propertyPanelApp = new DummyLayoutApp(SharedResources.skin, getAppName()) {
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
		};

		this.gridAppReference = propertyPanelApp;
	}

	@Override
	public void updateForGameAsset (GameAsset<IPropertyHolder> gameAsset) {
		super.updateForGameAsset(gameAsset);
		propertyPanel.showPanel(gameAsset.getResource(), gameAsset.getResource().getPropertyProviders());
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
}

