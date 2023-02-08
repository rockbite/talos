package com.talosvfx.talos.editor.addons.scene.widgets.directoryview;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.logic.PropertyWrapperProviders;
import com.talosvfx.talos.editor.utils.Toasts;
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
import com.talosvfx.talos.runtime.utils.NamingUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Supplier;

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

					if (fileHandle != null) {
						Array<FileHandle> selection = new Array<>();
						selection.add(fileHandle);
						projectExplorerWidget.showContextMenu(selection, true);
					}
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
				ObjectSet<IPropertyHolder> list = new ObjectSet<>();
				for (int i = 0; i < selected.size; i++) {
					Item item = selected.get(i);
					if (item.gameAsset != null) {
						if (!item.gameAsset.isBroken()) {
							RawAsset rootRawAsset = item.gameAsset.getRootRawAsset();
							AMetadata metaData = rootRawAsset.metaData;
							IPropertyHolder holderForMeta = PropertyWrapperProviders.getOrCreateHolder(metaData);
							list.add(holderForMeta);
						}
					}
				}
				if (list.isEmpty()) {
					holder = null;
				} else {
					holder = new MultiPropertyHolder<>(list);
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
					final FileHandle oldHandle = item.fileHandle;

					if (!oldHandle.isDirectory() && newText.isEmpty()) {
						newText = oldHandle.nameWithoutExtension();
					}
					if (!oldHandle.isDirectory() && !newText.endsWith(oldHandle.extension())) {
						newText += "." + oldHandle.extension();
					}

					FileHandle newHandle = item.fileHandle.parent().child(newText);
					if (newHandle.exists()) {
						if (!newHandle.name().equals(oldHandle.name())) {
							Toasts.getInstance().showErrorToast(
								"Cannot move asset from " + AssetRepository.relative(oldHandle) + " to " + AssetRepository.relative(newHandle)
									+ ".\n Destination path name does already exist.", Align.center
							);
						}
						item.setFile(oldHandle);
						return;
					}

					newHandle = AssetImporter.renameFile(oldHandle, newText);
					if (newHandle.isDirectory()) {
						projectExplorerWidget.notifyRename(oldHandle, newHandle);
					}
					item.setFile(newHandle);
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
					GlobalDragAndDrop.BaseDragAndDropPayload object = (GlobalDragAndDrop.BaseDragAndDropPayload)payload.getObject();

					boolean isSomethingWeWant = object instanceof GlobalDragAndDrop.FileHandleDragAndDropPayload || object instanceof GlobalDragAndDrop.GameAssetDragAndDropPayload || object instanceof GlobalDragAndDrop.ArrayDragAndDropPayload;

					if (!isSomethingWeWant)
						return false;

					return true;
				}

				@Override
				public void drop (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
					GlobalDragAndDrop.BaseDragAndDropPayload payloadObject = (GlobalDragAndDrop.BaseDragAndDropPayload)payload.getObject();
					FileHandle target = ((ProjectExplorerWidget.RowWidget) getActor()).getFileHandle();
					//We should check what kind of payload we got
					if (payloadObject.isExternal()) { // should copy file
						handlePayloadDropToDirectory(payloadObject, target, true);
					} else { // should move file
						handlePayloadDropToDirectory(payloadObject, target, false);
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

				FileHandle target;
				if (hit instanceof Item && isDirectory((Item) hit)) { // should move file into target directory
					Item dir = (Item) hit;
					target = dir.fileHandle;
				} else { // should move file into current directory
					target = fileHandle;
				}

				if (payloadObject.isExternal()) { // should copy file
					handlePayloadDropToDirectory(payloadObject, target, true);
				} else { // should move file
					handlePayloadDropToDirectory(payloadObject, target, false);
				}
			}
		});
	}

	/**
	 * Handle asset drop to directory view.
	 * @param payload FileHandle/GameAsset/ArrayOfPayloads, that was dropped to directory view.
	 * @param targetDir directory, where payload should be uploaded
	 * @param copy true - preserves original files and copies to target. false - original files are not preserved, but rather moved to target.
	 */
	private void handlePayloadDropToDirectory(GlobalDragAndDrop.BaseDragAndDropPayload payload, FileHandle targetDir, boolean copy) {
		if (!targetDir.isDirectory()) {
			return;
		}

		Array<FileHandle> files = new Array<>();

		if (payload instanceof GlobalDragAndDrop.ArrayDragAndDropPayload) {
			for (GlobalDragAndDrop.BaseDragAndDropPayload item : ((GlobalDragAndDrop.ArrayDragAndDropPayload) payload).getItems()) {
				if (item instanceof GlobalDragAndDrop.GameAssetDragAndDropPayload) {
					GameAsset<?> gameAsset = ((GlobalDragAndDrop.GameAssetDragAndDropPayload) item).getGameAsset();
					files.add(gameAsset.getRootRawAsset().handle);
				} else if (item instanceof GlobalDragAndDrop.FileHandleDragAndDropPayload) {
					files.add(((GlobalDragAndDrop.FileHandleDragAndDropPayload) item).getHandle());
				}
			}
		} else if (payload instanceof GlobalDragAndDrop.GameAssetDragAndDropPayload) {
			GameAsset<?> gameAsset = ((GlobalDragAndDrop.GameAssetDragAndDropPayload) payload).getGameAsset();
			files.add(gameAsset.getRootRawAsset().handle);
		} else if (payload instanceof GlobalDragAndDrop.FileHandleDragAndDropPayload) {
			files.add(((GlobalDragAndDrop.FileHandleDragAndDropPayload) payload).getHandle());
		}

		if (!files.isEmpty()) {
			asyncHandleFiles(files, targetDir, copy, 0);
		}
	}

	private void asyncHandleFiles(Array<FileHandle> files, FileHandle targetDir, boolean copy, int index) {
		FileHandle source = files.get(index);
		if (!targetDir.equals(source.parent())) {
			if (copy) {
				if (index == files.size - 1) { // last element should refresh directory view with changes
					copyFileHandle(source, targetDir, () -> {
						String projectPath = SharedResources.currentProject.rootProjectDir().path();
						projectExplorerWidget.loadDirectoryTree(projectPath);
						// Note: fileHandle is current directory open in view
						projectExplorerWidget.expand(fileHandle.path());
						projectExplorerWidget.select(fileHandle.path());
					});
				} else {
					copyFileHandle(source, targetDir,() -> {
						asyncHandleFiles(files, targetDir, copy, index + 1);
					});
				}
			} else {
				if (index == files.size - 1) {  // last element should refresh directory view with changes
					moveFileHandle(source, targetDir, () -> {
						String projectPath = SharedResources.currentProject.rootProjectDir().path();
						projectExplorerWidget.loadDirectoryTree(projectPath);
						// Note: fileHandle is current directory open in view
						projectExplorerWidget.expand(fileHandle.path());
						projectExplorerWidget.select(fileHandle.path());
					});
				} else {
					moveFileHandle(source, targetDir, () -> {
						asyncHandleFiles(files, targetDir, copy, index + 1);
					});
				}
			}
		}
	}

	private void copyFileHandle (FileHandle source, FileHandle target, Runnable onComplete) {
		if (target.child(source.name()).exists()) {
			// file is already present, see if it should be replaced
			String title = "Oh no!";
			String message = "An older item named \"" + source.name() + "\" already \n exists in this location. Do you want to replace it with the newer \n one you're moving?";
			Runnable keep = () -> {
				// copy new file to current directory with new name
				AssetRepository.getInstance().copyRawAsset(source, target, false);
				onComplete.run();
			};
			Runnable stop = () -> {
				// do nothing
				onComplete.run();
			};
			Runnable replace = () -> {
				// replace file in directory with new file
				AssetRepository.getInstance().copyRawAsset(source, target, true);
				onComplete.run();
			};

			projectExplorerWidget.showKeepStopReplaceDialog(title, message, keep, stop, replace);
		} else {
			AssetRepository.getInstance().copyRawAsset(source, target, false);
			onComplete.run();
		}
	}

	private void moveFileHandle (FileHandle source, FileHandle target, Runnable onComplete) {
		if (target.child(source.name()).exists()) {
			// file is already present, see if it should be replaced
			String title = "Oh no!";
			String message = "An older item named \"" + source.name() + "\" already \n exists in this location. Do you want to replace it with the newer \n one you're moving?";
			Runnable keep = () -> {
				// move new file to current directory with new name
				String fileName = NamingUtils.getNewName(source.nameWithoutExtension(), new Supplier<Collection<String>>() {
					@Override
					public Collection<String> get () {
						ArrayList<String> fileNames = new ArrayList<>();
						for (FileHandle fileHandle : target.list()) {
							fileNames.add(fileHandle.nameWithoutExtension());
						}
						return fileNames;
					}
				}) + "." + source.extension();

				AssetRepository.getInstance().moveFile(source, target.child(fileName), false);
				onComplete.run();
			};
			Runnable stop = () -> {
				// do nothing
				onComplete.run();
			};
			Runnable replace = () -> {
				// replace file in directory with new file
				AssetRepository.getInstance().moveFile(source, target, false);
				onComplete.run();
			};

			projectExplorerWidget.showKeepStopReplaceDialog(title, message, keep, stop, replace);
		} else {
			AssetRepository.getInstance().moveFile(source, target, false);
			onComplete.run();
		}
	}

	private static boolean isDirectory (Item item) {
		if (item == null || item.fileHandle == null) {
			return false;
		}
		return item.fileHandle.isDirectory();
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
