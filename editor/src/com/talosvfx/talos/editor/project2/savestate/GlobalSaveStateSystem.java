package com.talosvfx.talos.editor.project2.savestate;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.notifications.CommandEventHandler;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.commands.enums.Commands;
import com.talosvfx.talos.editor.notifications.events.ProjectUnloadEvent;
import com.talosvfx.talos.editor.notifications.events.ProjectUnloadEvent;
import com.talosvfx.talos.editor.notifications.events.commands.CommandEvent;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.RawAsset;
import com.talosvfx.talos.runtime.assets.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.utils.Toasts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;

public class GlobalSaveStateSystem implements Observer {

	private static final Logger logger = LoggerFactory.getLogger(GlobalSaveStateSystem.class);

    public static abstract class StateObject {

		private long counter = 0;
		private static long globalCounter = 1;

		private boolean persisted;

		StateObject () {
			counter = globalCounter++;
		}
		abstract void restore ();

		abstract StateObject beforeRestore ();
	}


	public static class MetaDataUpdateStateObject extends StateObject {
		private AMetadata metadata;
		private String asString;

		public MetaDataUpdateStateObject (AMetadata metadata) {
			super();
			this.metadata = metadata;

			FileHandle metaHandle = AssetImporter.getMetadataHandleFor(metadata.link.handle);
			asString = metaHandle.readString();
		}

		private MetaDataUpdateStateObject (AMetadata metadata, String data) {
			super();
			this.metadata = metadata;
			asString = data;
		}

		@Override
		void restore () {

			FileHandle metaHandle = AssetImporter.getMetadataHandleFor(metadata.link.handle);

			metaHandle.writeString(asString, false);

			AssetRepository.getInstance().reloadMetaData(metadata);

			Toasts.getInstance().showInfoToast("Undone " + metadata.getClass().getSimpleName() + " state");
		}

		@Override
		StateObject beforeRestore () {
			FileHandle metaHandle = AssetImporter.getMetadataHandleFor(metadata.link.handle);
			String before = metaHandle.readString();
			GlobalSaveStateSystem.MetaDataUpdateStateObject stateBeforeRestore = new GlobalSaveStateSystem.MetaDataUpdateStateObject(metadata, before);
			return stateBeforeRestore;
		}
	}
	public static class GameAssetUpdateStateObject extends StateObject {

		private GameAsset<?> gameAsset;
		private String asString;

		public GameAssetUpdateStateObject (GameAsset<?> gameAsset) {
			super();
			this.gameAsset = gameAsset;
			asString = SharedResources.globalSaveStateSystem.getAndIncrementLatestGameAssetAsString(gameAsset);
		}

		private GameAssetUpdateStateObject (GameAsset<?> gameAsset, String data) {
			super();
			this.gameAsset = gameAsset;
			asString = data;
		}

		@Override
		void restore () {
			SharedResources.globalSaveStateSystem.rawStringHistoryMap.put(gameAsset, asString);

			AssetRepository.getInstance().reloadGameAssetFromString(gameAsset, asString);

			Toasts.getInstance().showInfoToast("Undone " + gameAsset.getResource().getClass().getSimpleName() + " [" + gameAsset.type + "] state");
		}

		@Override
		StateObject beforeRestore () {
			String before = SharedResources.globalSaveStateSystem.rawStringHistoryMap.get(gameAsset);
			GlobalSaveStateSystem.GameAssetUpdateStateObject stateBeforeRestore = new GlobalSaveStateSystem.GameAssetUpdateStateObject(gameAsset, before);
			return stateBeforeRestore;
		}
	}


	private Stack<StateObject> undoStateObjects = new Stack<>();
	private Stack<StateObject> redoStateObjects = new Stack<>();
	private ObjectSet<GameAsset<?>> hasChanges = new ObjectSet<>();
	private ObjectMap<GameAsset<?>, String> rawStringHistoryMap = new ObjectMap<>();

	public GlobalSaveStateSystem () {
		Notifications.registerObserver(this);
	}

	public <T> boolean isItemChangedAndUnsaved (GameAsset<T> gameAsset) {
		return hasChanges.contains(gameAsset);
	}

	private String getAndIncrementLatestGameAssetAsString (GameAsset<?> gameAsset) {
		RawAsset rootRawAsset = gameAsset.getRootRawAsset();

		String returnString;

		if (rawStringHistoryMap.containsKey(gameAsset)) {
			//We use this latest one, return it, and then put in the current state as the string in cache
			returnString = rawStringHistoryMap.get(gameAsset);
		} else {
			//Use from file
			returnString = rootRawAsset.handle.readString();
		}

		//Put the current in memory representation for the next time
		String memoryRepresentation = AssetRepository.getInstance().saveGameAssetCurrentStateToJsonString(gameAsset);
		rawStringHistoryMap.put(gameAsset, memoryRepresentation);

		return returnString;
	}

	public void pushItem (StateObject assetUpdateStateObject) {
		redoStateObjects.clear();

		undoStateObjects.push(assetUpdateStateObject);
		if (assetUpdateStateObject instanceof GameAssetUpdateStateObject) {
			addToGameAssetStates((GameAssetUpdateStateObject) assetUpdateStateObject);
		}
	}

	private void addToGameAssetStates (GameAssetUpdateStateObject gameAssetUpdateStateObject) {
		hasChanges.add(gameAssetUpdateStateObject.gameAsset);
	}
	private void removeFromGameAssetStates (GameAssetUpdateStateObject gameAssetUpdateStateObject) {
		hasChanges.remove(gameAssetUpdateStateObject.gameAsset);
	}

	public void markSaved (GameAsset<?> gameAsset) {
		hasChanges.remove(gameAsset);
	}

	public void onUndoRequest () {
		if (undoStateObjects.isEmpty()) {
			Toasts.getInstance().showErrorToast("Nothing left to undo");
		} else {
			StateObject pop = undoStateObjects.pop();
			StateObject before = pop.beforeRestore();
			redoStateObjects.push(before);
			pop.restore();
		}
	}

	public void onRedoRequest () {
		if (redoStateObjects.isEmpty()) {
			Toasts.getInstance().showErrorToast("Nothing left to redo");
		} else {
			StateObject pop = redoStateObjects.pop();
			StateObject before = pop.beforeRestore();
			undoStateObjects.push(before);
			pop.restore();
		}
	}

	@CommandEventHandler(commandType = Commands.CommandType.UNDO)
	public void onUndoAction (CommandEvent actionEvent) {
		SharedResources.globalSaveStateSystem.onUndoRequest();
	}

	@CommandEventHandler(commandType = Commands.CommandType.REDO)
	public void onRedoAction (CommandEvent actionEvent) {
		SharedResources.globalSaveStateSystem.onRedoRequest();
	}

	@EventHandler
	public void onProjectUnload(ProjectUnloadEvent projectUnloadEvent) {
		redoStateObjects.clear();
		undoStateObjects.clear();
		hasChanges.clear();
		rawStringHistoryMap.clear();
	}

}
