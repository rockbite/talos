package com.talosvfx.talos.editor.project2.apps;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.talosvfx.talos.editor.NodeStage;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.ModuleBoardWidget;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;

public class ParticleNodeEditorApp extends AppManager.BaseApp<ParticleEffectDescriptor> {

	private final ModuleBoardWidget moduleBoardWidget;

	public ParticleNodeEditorApp () {
		this.singleton = false;

		moduleBoardWidget = new ModuleBoardWidget(null);
	}

	@Override
	public void updateForGameAsset (GameAsset<ParticleEffectDescriptor> gameAsset) {
		super.updateForGameAsset(gameAsset);

		this.gridAppReference = new DummyLayoutApp(SharedResources.skin, getAppName()) {
			@Override
			public Actor getMainContent () {
				return moduleBoardWidget;
			}
		};

		System.out.println();
	}

	@Override
	public String getAppName () {
		if (gameAsset != null) {
			return "VFX - " + gameAsset.nameIdentifier;
		} else {
			return "VFX - ";
		}
	}
}

