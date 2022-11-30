package com.talosvfx.talos.editor.project2.apps;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.serialization.VFXProjectData;
import com.talosvfx.talos.editor.widgets.ui.Preview3D;

public class ParticlePreviewApp extends AppManager.BaseApp<VFXProjectData> {


	private Preview3D preview3D;

	public ParticlePreviewApp () {
		this.singleton = false;

		preview3D = new Preview3D();

		this.gridAppReference = new DummyLayoutApp(SharedResources.skin, getAppName()) {
			@Override
			public Actor getMainContent () {
				return preview3D;
			}
			@Override
			public void onInputProcessorAdded () {
				super.onInputProcessorAdded();
				SharedResources.stage.setScrollFocus(preview3D);
			}
			@Override
			public void onInputProcessorRemoved () {
				super.onInputProcessorRemoved();
			}


		};
	}

	@Override
	public void updateForGameAsset (GameAsset<VFXProjectData> gameAsset) {
		super.updateForGameAsset(gameAsset);

		preview3D.setParticleEffect(gameAsset.getResource());

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

