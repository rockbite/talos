package com.talosvfx.talos.editor.project2.apps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.events.vfx.VFXPreviewActivated;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.notifications.events.deprecatedparticles.RegisterDragPoints;
import com.talosvfx.talos.editor.notifications.events.deprecatedparticles.UnRegisterDragPoints;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.serialization.VFXProjectData;
import com.talosvfx.talos.editor.widgets.ui.Preview3D;
import lombok.Getter;

public class ParticlePreviewApp extends AppManager.BaseApp<VFXProjectData> implements Observer {


	@Getter
	private Preview3D preview3D;

	public ParticlePreviewApp () {
		this.singleton = false;

		preview3D = new Preview3D();

		this.gridAppReference = new DummyLayoutApp<VFXProjectData>(SharedResources.skin, this, getAppName()) {
			@Override
			public Actor getMainContent () {
				return preview3D;
			}
			@Override
			public void onInputProcessorAdded () {
				super.onInputProcessorAdded();
				SharedResources.inputHandling.addPriorityInputProcessor(preview3D.getTinyGizmoRenderer().getInputAdapter());
				SharedResources.stage.setScrollFocus(preview3D);
				SharedResources.inputHandling.setGDXMultiPlexer();

			}
			@Override
			public void onInputProcessorRemoved () {
				SharedResources.inputHandling.removePriorityInputProcessor(preview3D.getTinyGizmoRenderer().getInputAdapter());
				super.onInputProcessorRemoved();
				SharedResources.inputHandling.setGDXMultiPlexer();

			}


		};

		Notifications.registerObserver(this);
	}

	@Override
	public void updateForGameAsset (GameAsset<VFXProjectData> gameAsset) {
		super.updateForGameAsset(gameAsset);

		// TODO: 23.02.23 dummy refactor
		if (AppManager.dummyAsset == (GameAsset) gameAsset) {
			return;
		}

		preview3D.setParticleEffect(gameAsset.getResource());

		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				Notifications.fireEvent(Notifications.obtainEvent(VFXPreviewActivated.class).set(gameAsset));
			}
		});
	}

	@Override
	public String getAppName () {
		if (gameAsset != null) {
			return "VFX - " + gameAsset.nameIdentifier;
		} else {
			return "VFX - ";
		}
	}

	@EventHandler
	public void onRegisterDragPoints (RegisterDragPoints registerDragPoints) {
		preview3D.registerForDragPoints(registerDragPoints.getRegisterForDragPoints());
	}

	@EventHandler
	public void onUnRegisterDragPoints (UnRegisterDragPoints registerDragPoints) {
		preview3D.unregisterDragPoints(registerDragPoints.getUnRegisterForDragPoints());
	}


	@Override
	public void onRemove () {
		Notifications.unregisterObserver(this);
	}
}

