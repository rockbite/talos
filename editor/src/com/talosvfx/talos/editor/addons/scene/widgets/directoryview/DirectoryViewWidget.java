package com.talosvfx.talos.editor.addons.scene.widgets.directoryview;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.logic.PropertyWrapperProviders;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.RawAsset;
import com.talosvfx.talos.editor.addons.scene.events.PropertyHolderSelected;
import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.logic.MultiPropertyHolder;
import com.talosvfx.talos.runtime.assets.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.FilePropertyProvider;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.widgets.ProjectExplorerWidget;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project2.GlobalDragAndDrop;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.ui.ActorCloneable;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

import static com.talosvfx.talos.editor.utils.InputUtils.ctrlPressed;

public class DirectoryViewWidget extends Table {

	private static final Logger logger = LoggerFactory.getLogger(DirectoryViewWidget.class);
	private static final DirectoryViewFileComparator DIRECTORY_VIEW_FILE_COMPARATOR = new DirectoryViewFileComparator();
	private static final FileFilter DIRECTORY_VIEW_FILE_FILTER = new DirectoryViewFileFilter();
	private final ScrollPane scrollPane;
	private final ProjectExplorerWidget projectExplorerWidget;

	private Array<Item> selected = new Array<>();

	private ItemGroup items;
	private Table emptyFolderTable;

	private FileHandle fileHandle;

	public DirectoryViewWidget (ProjectExplorerWidget projectExplorerWidget) {
		this.projectExplorerWidget = projectExplorerWidget;

		emptyFolderTable = new Table();
		Label emptyFolder = new Label("This folder is empty.", SharedResources.skin);
		emptyFolderTable.add(emptyFolder).expand().center().top().padTop(20);

		items = new ItemGroup();

		addListener(new ClickListener(0) {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				if (!event.isStopped()) {
					clearSelection();
				}
			}
		});

		addListener(new ClickListener(1) {

			@Override
			public void clicked (InputEvent event, float x, float y) {
				if (!event.isStopped()) {
					clearSelection();

					Array<FileHandle> selection = new Array<>();
					selection.add(fileHandle);

					projectExplorerWidget.showContextMenu(selection, true);
					event.stop();
				}
			}

		});

		items.setCellSize(50);
		items.pad(20);
		items.wrapSpace(10);
		items.space(10);

		scrollPane = new ScrollPane(items);
		scrollPane.setScrollbarsVisible(true);
		Stack stack = new Stack(scrollPane, emptyFolderTable);
		add(stack).grow().height(0).row();
		Slider slider = new Slider(50, 125, 1, false, SharedResources.skin);

		slider.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				items.setCellSize(slider.getValue());
				items.invalidateHierarchy();
			}
		});
		add(slider).width(125).pad(5, 10, 5, 10).expandX().right();

		slider.setValue(50f + (125 - 50) / 2f);
		items.setCellSize(slider.getValue());
		items.invalidateHierarchy();

		setTouchable(Touchable.enabled);
	}


	public void invokeCut() {
		projectExplorerWidget.invokeCut(convertToFileArray(selected));
	}

	public void invokeCopy() {
		projectExplorerWidget.invokeCopy(convertToFileArray(selected));
	}

	public void invokePaste() {
		projectExplorerWidget.invokePaste(fileHandle);
	}

	public void invokeRename() {
		rename();
	}

	public void invokeDelete() {
		Array<String> paths = new Array<>();
		for (int i = 0; i < selected.size; i++) {
			Item item = selected.get(i);
			paths.add(item.getFileHandle().path());
		}
		projectExplorerWidget.deletePath(paths);
	}

	public void invokeSelectAll() {
		for (Actor child : items.getChildren()) {
			Item item = (Item)child;
			if (!selected.contains(item, true)) {
				item.select();
				selected.add(item);
			}
		}
		reportSelectionChanged();
	}

	private void reportSelectionChanged () {
		if (!selected.isEmpty()) {
			IPropertyHolder holder = null;
			if (selected.size == 1) {
				Item item = selected.first();
				if (item.gameAsset != null) {
					if (!item.gameAsset.isBroken()) {
						AMetadata metaData = item.gameAsset.getRootRawAsset().metaData;
						IPropertyHolder holderForMeta = PropertyWrapperProviders.getOrCreateHolder(metaData);
						holder = holderForMeta;
					}
				} else {
					if (item.fileHandle != null) {
						IPropertyProvider provider = new FilePropertyProvider(item.fileHandle);
						holder = new IPropertyHolder() {
							@Override
							public Iterable<IPropertyProvider> getPropertyProviders() {
								Array<IPropertyProvider> arr = new Array<>();
								arr.add(provider);
								return arr;
							}

							@Override
							public String getName() {
								return item.fileHandle.name();
							}
						};
					}
				}
			} else if (selected.size > 1) {
				ObjectSet<AMetadata> list = new ObjectSet<AMetadata>();
				for (int i = 0; i < selected.size; i++) {
					Item item = selected.get(i);
					if (item.gameAsset != null) {
						if (!item.gameAsset.isBroken()) {
							RawAsset rootRawAsset = item.gameAsset.getRootRawAsset();
							list.add(rootRawAsset.metaData);
						}
					}
				}
				if (list.isEmpty()) {
					holder = null;
				} else {
					holder = new MultiPropertyHolder(list);
				}
			}

			if (holder != null) {
				Notifications.fireEvent(Notifications.obtainEvent(PropertyHolderSelected.class).setTarget(holder));
			}
		}
	}

	public void rename () {
		if (selected.size >= 1) {
			Item item = selected.first();

			clearSelection();
			selected.add(item);
			item.select();

			item.rename();
		}
	}

	/**
	 * Open directory in current view.
	 *
	 * @param path Path of the current folder. Can be both absolute or relative.
	 */
	public void openDirectory (String path) {
		FileHandle directory = Gdx.files.absolute(path);
		if (!directory.exists()) { // check if file exists
			System.out.println("Error opening directory: " + path);
			return;
		}

		if (!directory.isDirectory()) { // check if it's a folder
			System.out.println("Error provided path is not directory: " + path);
		}

		fileHandle = Gdx.files.absolute(path);

		fillItems(directory);
		invalidateHierarchy();
		layout();
	}

	private Array<FileHandle> convertToFileArray (Array<Item> selected) {
		Array<FileHandle> handles = new Array<>();
		for (int i = 0; i < selected.size; i++) {
			Item item = selected.get(i);
			handles.add(item.getFileHandle());
		}

		return handles;
	}

	/**
	 * Clears old items and populates the view with items in current directory.
	 * Indicates if the folder is empty.
	 *
	 * @param directory directory exists and it's directory.
	 */
	private void fillItems (FileHandle[] directory) {
		// reset state
		selected.clear();
		items.clear();

		if (directory.length == 0) {
			return;
		}

		//BEGIN DRAG AND DROP
		dragAndDrop();
		////END DRAG AND DROP

		FileHandle[] content = directory;
		if (content.length == 0) {
			emptyFolderTable.setVisible(true);
			items.setVisible(false);
			return;
		} else {
			emptyFolderTable.setVisible(false);
			items.setVisible(true);
		}

		Arrays.sort(content, DIRECTORY_VIEW_FILE_COMPARATOR);

		for (FileHandle fileHandle : content) {
			if (!DIRECTORY_VIEW_FILE_FILTER.accept(fileHandle.file())) {
				continue; // skip over unwanted files
			}
			Item item = new Item();
			item.addListener(new ClickListener() {

				@Override
				public void clicked (InputEvent event, float x, float y) {
					event.stop();
					if (getTapCount() == 1) {
						//Report the click
						itemClicked(item, false);
						reportSelectionChanged();
					} else if (getTapCount() == 2) {
						//Report the double click
						itemDoubleClicked(item);
						reportSelectionChanged();
					}

				}
			});
			item.addListener(new ClickListener(1) {
				@Override
				public void clicked (InputEvent event, float x, float y) {
					event.stop();
					//Report the click
					itemClicked(item, true);
					reportSelectionChanged();
				}
			});
			item.setFile(fileHandle);

			EditableLabel itemEditableLabel = item.label;

			itemEditableLabel.setListener(new EditableLabel.EditableLabelChangeListener() {

				@Override
				public void editModeStarted () {

				}

				@Override
				public void changed (String newText) {
					if (newText.isEmpty()) {
						newText = item.fileHandle.nameWithoutExtension();
					}
					FileHandle newHandle = AssetImporter.renameFile(item.fileHandle, newText);
					if (newHandle.isDirectory()) {
						projectExplorerWidget.notifyRename(item.fileHandle, newHandle);
					}
					item.fileHandle = newHandle;
					item.setFile(item.fileHandle);
				}
			});

			items.addActor(item);

			SharedResources.globalDragAndDrop.addSource(new DragAndDrop.Source(item) {
				@Override
				public DragAndDrop.Payload dragStart (InputEvent event, float x, float y, int pointer) {
					DragAndDrop.Payload payload = new DragAndDrop.Payload();

					if (!selected.contains(item, true)) {
						selected.clear();
						selected.add(item);
					}

					if (selected.size == 1) {
						Item newView = new Item();
						Actor dragging = ((ActorCloneable)newView).copyActor(item);
						dragging.setSize(item.getWidth(), item.getHeight());
						payload.setDragActor(dragging);
						payload.setObject(getPayloadForItem(item));
					} else {
						Label dragging = new Label("Multiple selection", SharedResources.skin);
						payload.setDragActor(dragging);
						payload.setObject(convertArrayIntoArrayDragDropPayload(selected));
					}

					return payload;
				}
			});
		}


		for (ObjectMap.Entry<String, FilteredTree.Node<String>> node : projectExplorerWidget.getNodes()) {
			SharedResources.globalDragAndDrop.addTarget(new DragAndDrop.Target(node.value.getActor()) {
				@Override
				public boolean drag (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
					return true;
				}

				@Override
				public void drop (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
					GlobalDragAndDrop.BaseDragAndDropPayload object = (GlobalDragAndDrop.BaseDragAndDropPayload)payload.getObject();

					boolean isSomethingWeWant = object instanceof GlobalDragAndDrop.FileHandleDragAndDropPayload || object instanceof GlobalDragAndDrop.GameAssetDragAndDropPayload || object instanceof GlobalDragAndDrop.ArrayDragAndDropPayload;

					if (!isSomethingWeWant)
						return;

					FileHandle destination = ((ProjectExplorerWidget.RowWidget) getActor()).getFileHandle();
					if (object instanceof GlobalDragAndDrop.ArrayDragAndDropPayload) {
						GlobalDragAndDrop.ArrayDragAndDropPayload payloadObject = (GlobalDragAndDrop.ArrayDragAndDropPayload) object;
						for (GlobalDragAndDrop.BaseDragAndDropPayload item : payloadObject.getItems()) {
							if (item instanceof GlobalDragAndDrop.GameAssetDragAndDropPayload) {
								handlePayloadObject((GlobalDragAndDrop.GameAssetDragAndDropPayload) item);
							} else if (item instanceof GlobalDragAndDrop.FileHandleDragAndDropPayload) {
								handlePayloadObject((GlobalDragAndDrop.FileHandleDragAndDropPayload) item);
							}
						}
						navigateTo(destination);
					} else if (object instanceof GlobalDragAndDrop.GameAssetDragAndDropPayload) {
						handlePayloadObject((GlobalDragAndDrop.GameAssetDragAndDropPayload) object);
						navigateTo(destination);
					} else if (object instanceof GlobalDragAndDrop.FileHandleDragAndDropPayload) {
						handlePayloadObject((GlobalDragAndDrop.FileHandleDragAndDropPayload) object);
						navigateTo(destination);
					}
				}

				private void handlePayloadObject(GlobalDragAndDrop.GameAssetDragAndDropPayload payload) {
					GameAsset<?> sourceItem = payload.getGameAsset();
					FileHandle handle = sourceItem.getRootRawAsset().handle;
					FileHandle destination = ((ProjectExplorerWidget.RowWidget) getActor()).getFileHandle();
					if (!handle.parent().path().equals(destination.path())) {
						AssetRepository.getInstance().moveFile(handle, destination, true, false);
					}
				}

				private void handlePayloadObject(GlobalDragAndDrop.FileHandleDragAndDropPayload payload) {
					FileHandle handle = payload.getHandle();
					FileHandle destination = ((ProjectExplorerWidget.RowWidget) getActor()).getFileHandle();
					if (!handle.parent().path().equals(destination.path())) {
						AssetRepository.getInstance().moveFile(handle, destination, true, false);
					}
				}
			});
		}
	}

	private GlobalDragAndDrop.BaseDragAndDropPayload getPayloadForItem (Item item) {
		if (item.gameAsset != null) {
			return new GlobalDragAndDrop.GameAssetDragAndDropPayload(item.gameAsset);
		} else {
			return new GlobalDragAndDrop.FileHandleDragAndDropPayload(item.fileHandle);
		}
	}

	private GlobalDragAndDrop.ArrayDragAndDropPayload convertArrayIntoArrayDragDropPayload (Array<Item> selected) {
		GlobalDragAndDrop.ArrayDragAndDropPayload arrayDragAndDropPayload = new GlobalDragAndDrop.ArrayDragAndDropPayload();

		for (Item item : selected) {
			GlobalDragAndDrop.BaseDragAndDropPayload payloadforItem = getPayloadForItem(item);
			arrayDragAndDropPayload.getItems().add(payloadforItem);
		}

		return arrayDragAndDropPayload;
	}

	private void dragAndDrop () {
		SharedResources.globalDragAndDrop.addTarget(new DragAndDrop.Target(this) {
			@Override
			public boolean drag (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
				GlobalDragAndDrop.BaseDragAndDropPayload object = (GlobalDragAndDrop.BaseDragAndDropPayload)payload.getObject();

				boolean isSomethingWeWant = object instanceof GlobalDragAndDrop.FileHandleDragAndDropPayload || object instanceof GlobalDragAndDrop.GameAssetDragAndDropPayload || object instanceof GlobalDragAndDrop.ArrayDragAndDropPayload;

				if (!isSomethingWeWant)
					return false;

				Actor hit = DirectoryViewWidget.this.hit(x, y, true);

				for (Actor child : items.getChildren()) {
					((Item)child).setMouseover(false);
				}

				if (hit instanceof Item) {
					((Item)hit).setMouseover(true);
					return true;
				}

				return true;
			}

			@Override
			public void drop (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
				for (Actor child : items.getChildren()) {
					((Item)child).setMouseover(false);
				}

				Actor hit = DirectoryViewWidget.this.hit(x, y, true);


				//We should check what kind of payload we got

				GlobalDragAndDrop.BaseDragAndDropPayload payloadObject = (GlobalDragAndDrop.BaseDragAndDropPayload)payload.getObject();


				if (hit instanceof Item) {
					Item targetItem = (Item)hit; //todo

					if (!targetItem.fileHandle.isDirectory() || targetItem.fileHandle.path().equals(fileHandle.path())) {
						return;
					}

					handlePayloadMove(payloadObject, targetItem);

					for (Item item : selected) {
						item.deselect();
					}
					selected.clear();
					reportSelectionChanged();

					openDirectory(targetItem.fileHandle.path());
				} else {
					if (payloadObject instanceof GlobalDragAndDrop.FileHandleDragAndDropPayload) {
						handleDragAndDrop((GlobalDragAndDrop.FileHandleDragAndDropPayload) payloadObject);
					} else if (payloadObject instanceof GlobalDragAndDrop.ArrayDragAndDropPayload) {
						handleDragAndDrop((GlobalDragAndDrop.ArrayDragAndDropPayload) payloadObject);
					}
				}
			}
			private void handleDragAndDrop (GlobalDragAndDrop.ArrayDragAndDropPayload payloadObjects) {
				Array<GlobalDragAndDrop.BaseDragAndDropPayload> items = payloadObjects.getItems();
				FileHandle lastCopiedFile = null;
				for (GlobalDragAndDrop.BaseDragAndDropPayload item : items) {
					if (item instanceof GlobalDragAndDrop.FileHandleDragAndDropPayload) {
						FileHandle file = handle((GlobalDragAndDrop.FileHandleDragAndDropPayload) item);
						if (file != null) {
							lastCopiedFile = file;
						}
					}
				}

				if (lastCopiedFile != null) {
					// open current directory
					navigateTo(lastCopiedFile.parent());
				}
			}

			private void handleDragAndDrop (GlobalDragAndDrop.FileHandleDragAndDropPayload payloadObject) {
				FileHandle copiedFile = handle(payloadObject);
				if (copiedFile != null) {
					// open current directory
					navigateTo(copiedFile.parent().parent());
				}
			}

			private FileHandle handle (GlobalDragAndDrop.FileHandleDragAndDropPayload item) {
				AssetRepository assetRepository = AssetRepository.getInstance();
				FileHandle handle = item.getHandle();
				if (fileHandle.child(handle.name()).exists()) {
					// file is already present, see if it should be replaced
					String title = "Oh no!";
					String message = "An older item named \"" + handle.name() + "\" already \n exists in this location. Do you want to replace it with the newer \n one you're moving?";
					Runnable keep = () -> {
						// copy new file to current directory with new name
						FileHandle newHandle = assetRepository.copyRawAsset(handle, fileHandle);
						String projectPath = SharedResources.currentProject.rootProjectDir().path();
						projectExplorerWidget.loadDirectoryTree(projectPath);
						projectExplorerWidget.expand(newHandle.parent().path());
						projectExplorerWidget.select(newHandle.parent().path());
					};
					Runnable stop = () -> {
						// do nothing
					};
					Runnable replace = () -> {
						// replace file in directory with new file
						FileHandle newHandle = assetRepository.copyRawAsset(handle, fileHandle, true);
						String projectPath = SharedResources.currentProject.rootProjectDir().path();
						projectExplorerWidget.loadDirectoryTree(projectPath);
						projectExplorerWidget.expand(newHandle.parent().path());
						projectExplorerWidget.select(newHandle.parent().path());
					};

					projectExplorerWidget.showKeepStopReplaceDialog(title, message, keep, stop, replace);
					return null;
				} else {
					return assetRepository.copyRawAsset(handle, fileHandle);
				}
			}
		});
	}

	private static void handlePayloadMove (GlobalDragAndDrop.BaseDragAndDropPayload payloadObject, Item targetItem) {
		if (payloadObject instanceof GlobalDragAndDrop.GameAssetDragAndDropPayload) {
			//Do single drag and drop asset move
			GameAsset<?> gameAsset = ((GlobalDragAndDrop.GameAssetDragAndDropPayload)payloadObject).getGameAsset();
			FileHandle handle = gameAsset.getRootRawAsset().handle;
			if (!handle.path().equals(targetItem.fileHandle.path())) {
				AssetImporter.moveFile(handle, targetItem.fileHandle, false);
			}
		} else if (payloadObject instanceof GlobalDragAndDrop.FileHandleDragAndDropPayload) {
			FileHandle handle = ((GlobalDragAndDrop.FileHandleDragAndDropPayload)payloadObject).getHandle();
			if (!handle.path().equals(targetItem.fileHandle.path())) {
				AssetImporter.moveFile(handle, targetItem.fileHandle, false);
			}
		} else if (payloadObject instanceof GlobalDragAndDrop.ArrayDragAndDropPayload) {
			Array<GlobalDragAndDrop.BaseDragAndDropPayload> items = ((GlobalDragAndDrop.ArrayDragAndDropPayload)payloadObject).getItems();
			Array<GlobalDragAndDrop.BaseDragAndDropPayload> itemCopy = new Array<>();
			itemCopy.addAll(items);
			for (int i = 0; i < itemCopy.size; i++) {
				handlePayloadMove(itemCopy.get(i), targetItem);
			}
		}
	}

	private void itemClicked (Item item, boolean rightClick) {
		boolean addToSelection = ctrlPressed();
		boolean shiftSelectRange = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);

		if (rightClick) {
			// if it's selected and possibly others, keep the entire selection
			if(!selected.contains(item, true)) {
				clearSelection();
				selected.add(item);
				item.select();
			}
			Array<FileHandle> handles = convertToFileArray(selected);

			projectExplorerWidget.showContextMenu(handles, true);

			return;
		}

		if (selected.contains(item, true)) {

			if (!addToSelection && !shiftSelectRange) {
				clearSelection();

				//Add it back
				selected.add(item);
				item.select();

			} else {

				selected.removeValue(item, true);
				item.deselect();
			}

		} else {
			if (shiftSelectRange && !selected.isEmpty()) {
				//Find the indexes and select all the range from the latest added to the selection as reerence
				Item latestAdded = selected.peek();

				SnapshotArray<Actor> children = items.getChildren();

				int firstIndex = children.indexOf(latestAdded, true);
				int indexOfTarget = children.indexOf(item, true);

				//We add all these that are in the range
				if (firstIndex > indexOfTarget) {
					for (int i = indexOfTarget; i < firstIndex; i++) {
						Item actor = (Item)children.get(i);
						if (!selected.contains(actor, true)) {
							selected.add(actor);
							actor.select();
						}
					}
				} else if (firstIndex < indexOfTarget) {
					for (int i = firstIndex; i <= indexOfTarget; i++) {
						Item actor = (Item)children.get(i);
						if (!selected.contains(actor, true)) {
							selected.add(actor);
							actor.select();
						}
					}
				} else {
					//Do nothing we selected same thing
				}

			} else {
				if (!addToSelection) {
					clearSelection();
				}
				item.select();
				selected.add(item);
			}
		}
	}

	private void clearSelection () {
		for (Item item : selected) {
			item.deselect();
		}
		selected.clear();
	}

	private void itemDoubleClicked (Item item) {
		clearSelection();
		selected.add(item);
		item.select();
		AssetImporter.fileOpen(item.getFileHandle());
	}

	public void fillItems (FileHandle directory) {
		fillItems(directory.list());
	}

	public void fillItems (Array<FileHandle> directory) {
		fillItems(directory.toArray(FileHandle.class));
	}

	public void selectForPath (FileHandle newHandle) {
		Item found = null;
		for (Actor child : items.getChildren()) {
			FileHandle testHandle = ((Item)child).getFileHandle();
			if (testHandle.equals(newHandle)) {
				found = (Item)child;
				break;
			}
		}
		if (found != null) {
			clearSelection();
			found.select();
			selected.add(found);
		}
	}

	public void scrollTo (FileHandle newHandle) {
		SnapshotArray<Actor> children = items.getChildren();
		Item found = null;
		for (Actor child : children) {
			Item item = (Item)child;
			if (item.fileHandle.equals(newHandle)) {
				found = item;
				break;
			}
		}
		if (found != null) {

			float topY = scrollPane.getScrollY();
			float scrollHeight = scrollPane.getScrollHeight();

			float positionInParent = items.getHeight() - (found.getY() + found.getHeight() / 2f);

			if (positionInParent < topY || positionInParent > (topY + scrollHeight)) {
				scrollPane.setScrollY(positionInParent - scrollHeight / 2f);
			}

		}
	}

	public ScrollPane getScrollPane () {
		return scrollPane;
	}

	/**
	 * Compares two FileHandles and sorts them in alphabetical order based on their names. Gives priority to
	 * directory if names are equal.
	 */
	private static class DirectoryViewFileComparator implements Comparator<FileHandle> {

		@Override
		public int compare (FileHandle o1, FileHandle o2) {
			if (o1.isDirectory() && !o2.isDirectory()) {
				return -1;
			} else if (o2.isDirectory() && !o1.isDirectory()) {
				return 1;
			}

			return o1.name().toUpperCase().compareTo(o2.name().toUpperCase());
		}
	}

	/**
	 * Hides unnecessary files.
	 */
	private static class DirectoryViewFileFilter implements FileFilter {

		@Override
		public boolean accept (File pathname) {

			if(pathname.getAbsolutePath().endsWith(".tse")) return false;
			if(pathname.getName().equals(".DS_Store")) return false;
			if(pathname.getAbsolutePath().endsWith(".meta")) return false;
			if(pathname.getAbsolutePath().endsWith(".p")) return false;
			if(pathname.getName().endsWith(".tlsprj")) return false;

			return true;
		}
	}

	public FileHandle getCurrentFolder () {
		return fileHandle;
	}

	private void navigateTo (FileHandle destination) {
		String projectPath = SharedResources.currentProject.rootProjectDir().path();
		projectExplorerWidget.loadDirectoryTree(projectPath);
		projectExplorerWidget.expand(destination.path());
		projectExplorerWidget.select(destination.path());
	}

	public void changeAssetPreview (FileHandle assetFileHande) {
		for (Actor child : items.getChildren()) {
			final Item item = (Item) child;
			if (item.fileHandle.equals(assetFileHande)) {
				item.updatePreview();
			}
		}
	}
}
