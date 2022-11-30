package com.talosvfx.talos.editor.project2.savestate;

import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.RawAsset;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.utils.Toasts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;

public class GlobalSaveStateSystem implements Observer {

	private static final Logger logger = LoggerFactory.getLogger(GlobalSaveStateSystem.class);

	public static abstract class StateObject {
		abstract void restore ();
	}


	public static class GameAssetUpdateStateObject extends StateObject {

		private GameAsset<?> gameAsset;
		private String asSTring;

		public GameAssetUpdateStateObject (GameAsset<?> gameAsset) {
			this.gameAsset = gameAsset;

			RawAsset rootRawAsset = this.gameAsset.getRootRawAsset();
			asSTring = rootRawAsset.handle.readString();
		}

		@Override
		void restore () {
			RawAsset rootRawAsset = gameAsset.getRootRawAsset();

			rootRawAsset.handle.writeString(asSTring, false);

			AssetRepository.getInstance().reloadGameAsset(gameAsset);

			Toasts.getInstance().showInfoToast("Undone " + gameAsset.getResource() + " [" + gameAsset.type + "] state");
		}
	}


	private Stack<StateObject> stateObjects = new Stack<>();

	public GlobalSaveStateSystem () {
		Notifications.registerObserver(this);
	}

	public void pushItem (GameAssetUpdateStateObject assetUpdateStateObject) {
		stateObjects.push(assetUpdateStateObject);
	}
	public void onUndoRequest () {
		if (stateObjects.isEmpty()) {
			Toasts.getInstance().showErrorToast("Nothing left to undo");
		} else {
			StateObject pop = stateObjects.pop();
			pop.restore();
		}
	}

}
