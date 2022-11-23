package com.talosvfx.talos.editor.addons.scene.widgets.directoryview;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.TalosInputProcessor;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.RawAsset;
import com.talosvfx.talos.editor.addons.scene.events.PropertyHolderSelected;
import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.logic.MultiPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.widgets.ProjectExplorerWidget;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.ActorCloneable;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

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

    private DragAndDrop dragAndDrop;
    private Array<DragAndDrop.Target> externalTargets;


    public DirectoryViewWidget (ProjectExplorerWidget projectExplorerWidget) {
        this.projectExplorerWidget = projectExplorerWidget;

        emptyFolderTable = new Table();
        Label emptyFolder = new Label("This folder is empty.", SharedResources.skin);
        emptyFolderTable.add(emptyFolder).expand().center().top().padTop(20);

        items = new ItemGroup();

        dragAndDrop = new DragAndDrop();
        externalTargets = new Array<>();

        addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (getStage() != null) {
                    getStage().setKeyboardFocus(DirectoryViewWidget.this);
                }
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {


                if (keycode == Input.Keys.X && TalosInputProcessor.ctrlPressed()) {
                    projectExplorerWidget.invokeCut(convertToFileArray(selected));
                }

                if (keycode == Input.Keys.C && TalosInputProcessor.ctrlPressed()) {
                    projectExplorerWidget.invokeCopy(convertToFileArray(selected));
                }

                if (keycode == Input.Keys.V && TalosInputProcessor.ctrlPressed()) {
                    projectExplorerWidget.invokePaste(fileHandle);
                }

                if (keycode == Input.Keys.FORWARD_DEL || keycode == Input.Keys.DEL) {
                    Array<String> paths = new Array<>();
                    for (int i = 0; i < selected.size; i++) {
                        Item item = selected.get(i);
                        paths.add(item.getFileHandle().path());
                    }
                    projectExplorerWidget.deletePath(paths);
                }

                if (keycode == Input.Keys.A && TalosInputProcessor.ctrlPressed()) {
                    for (Actor child : items.getChildren()) {
                        Item item = (Item) child;
                        if (!selected.contains(item, true)) {
                            item.select();
                            selected.add(item);
                        }
                    }
                    reportSelectionChanged();
                }

                boolean renamePressed = TalosMain.Instance().isOsX() && keycode == Input.Keys.ENTER ||
                                        !TalosMain.Instance().isOsX() && keycode == Input.Keys.F2;
                if (renamePressed) {
                    rename();
                }

                return true;
            }
        });


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
            public void changed(ChangeEvent event, Actor actor) {
                items.setCellSize(slider.getValue());
                items.invalidateHierarchy();
            }
        });
        add(slider).width(125).pad(5, 10, 5, 10).expandX().right();

        slider.setValue(50f + (125 - 50)/2f);
        items.setCellSize(slider.getValue());
        items.invalidateHierarchy();

        setTouchable(Touchable.enabled);
    }

    private void reportSelectionChanged() {
        if(!selected.isEmpty()) {
            IPropertyHolder holder = null;
            if (selected.size == 1) {
                Item item = selected.first();
                if (item.gameAsset != null) {
                    if (!item.gameAsset.isBroken()) {
                        holder = item.gameAsset.getRootRawAsset().metaData;
                    }
                }
            } else if (selected.size > 1) {
                ObjectSet<AMetadata> list = new ObjectSet<AMetadata>();
                for (int i = 0; i < selected.size; i ++) {
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
        if (selected.size == 1) {
            Item item = selected.first();
            item.rename();
        }
    }

    /**
     * Open directory in current view.
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
     * @param directory directory exists and it's directory.
     */
    private void fillItems (FileHandle[] directory) {
        // reset state
        selected.clear();
        items.clear();
        dragAndDrop.clear();

        if (directory.length == 0) {
            return;
        }

        //BEGIN DRAG AND DROP
        for (DragAndDrop.Target externalTarget : externalTargets) {
            dragAndDrop.addTarget(externalTarget);
        }

        //todo
//        dragAndDrop();

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
                public void clicked(InputEvent event, float x, float y) {
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
                public void changed(String newText) {
                    if(newText.isEmpty()) {
                        newText = item.fileHandle.nameWithoutExtension();
                    }
                    FileHandle newHandle = AssetImporter.renameFile(item.fileHandle, newText);
                    if(newHandle.isDirectory()) {
                        projectExplorerWidget.notifyRename(item.fileHandle, newHandle);
                    }
                    item.fileHandle = newHandle;
                    item.setFile(item.fileHandle);
                }
            });

            items.addActor(item);

            dragAndDrop.addSource(new DragAndDrop.Source(item) {
                @Override
                public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                    DragAndDrop.Payload payload = new DragAndDrop.Payload();


                    if(!selected.contains(item, true)) {
                        selected.clear();
                        selected.add(item);
                    }

                    if(selected.size == 1) {
                        Item newView = new Item();
                        Actor dragging = ((ActorCloneable) newView).copyActor(item);
                        dragging.setSize(item.getWidth(), item.getHeight());
                        payload.setDragActor(dragging);
                        if (newView.gameAsset != null) {
                            payload.setObject(newView.gameAsset);
                        } else {
                            payload.setObject(fileHandle);
                        }
                    } else {
                        Label dragging = new Label("Multiple selection", SharedResources.skin);
                        payload.setDragActor(dragging);
                        payload.setObject(selected);
                    }

                    return payload;
                }
            });
        }
    }

    private void dragAndDrop () {
//        dragAndDrop.addTarget(new DragAndDrop.Target(SceneEditorAddon.get().workspace) {
//            @Override
//            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
//
//                Actor actor = getActor();
//
//                return true;
//            }
//
//            @Override
//            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
//                Object object = payload.getObject();
//                if(object instanceof Array) {
//
//                    Array<Item> array = (Array<Item>) object;
//
//                    for (int i = 0; i < selected.size; i++) {
//                        Item item = selected.get(i);
//                        if (item.gameAsset != null) {
//                            AssetImporter.createAssetInstance(item.gameAsset, SceneEditorAddon.get().workspace.getRootGO());
//                        }
//                    }
//                } else {
//                    if (object instanceof GameAsset) {
//                        AssetImporter.createAssetInstance((GameAsset) payload.getObject(), SceneEditorAddon.get().workspace.getRootGO());
//                    }
//                    if (object instanceof FileHandle) {
//                        //Do nothing here anymore
//                    }
//                }
//
//                for (Item item : selected) {
//                    item.deselect();
//                }
//                selected.clear();
//                reportSelectionChanged();
//            }
//        });
//
//        dragAndDrop.addTarget(new DragAndDrop.Target(SceneEditorAddon.get().hierarchy) {
//            @Override
//            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
//                return true;
//            }
//
//            @Override
//            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
//                Actor targetActor = getActor();
//            }
//        });
//
//        for (ObjectMap.Entry<String, FilteredTree.Node<String>> node : SceneEditorAddon.get().projectExplorer.getNodes()) {
//            dragAndDrop.addTarget(new DragAndDrop.Target(node.value.getActor()) {
//                @Override
//                public boolean drag (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
//                    return true;
//                }
//
//                @Override
//                public void drop (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
//
//                    // Payload is either an Array<ItemView>, Or its a FileHandle, Or its a GameAsset
//
//                    fileHandle = ((ProjectExplorerWidget.RowWidget) getActor()).getFileHandle();
//
//                    Object object = payload.getObject();
//                    if (object instanceof Array) {
//                        Array<Item> array = (Array<Item>) object;
//                        for (Item sourceItem : array) {
//                            if (!sourceItem.fileHandle.path().equals(fileHandle.path())) {
//                                AssetImporter.moveFile(sourceItem.fileHandle, fileHandle, false);
//                            }
//                        }
//                    } else if (object instanceof GameAsset) {
//                        GameAsset<?> sourceItem = (GameAsset) payload.getObject();
//                        FileHandle handle = sourceItem.getRootRawAsset().handle;
//                        if (!handle.path().equals(fileHandle.path())) {
//                            AssetImporter.moveFile(handle, fileHandle, false);
//                        }
//                    } else if (object instanceof FileHandle) {
//                        FileHandle handle = (FileHandle) payload.getObject();
//                        if (!handle.path().equals(fileHandle.path())) {
//                            AssetImporter.moveFile(handle, fileHandle, false);
//                        }
//                    }
//                }
//            });
//        }
//
//        dragAndDrop.addTarget(new DragAndDrop.Target(this) {
//        @Override
//        public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
//            Actor hit = DirectoryViewWidget.this.hit(x, y, true);
//
//            for (Actor child : items.getChildren()) {
//                ((Item)child).setMouseover(false);
//            }
//
//            if (hit instanceof Item) {
//                ((Item)hit).setMouseover(true);
//                return true;
//            }
//
//            return false;
//        }
//
//        @Override
//        public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
//            for (Actor child : items.getChildren()) {
//                ((Item)child).setMouseover(false);
//            }
//
//            Actor hit = DirectoryViewWidget.this.hit(x, y, true);
//
//            if (!(hit instanceof Item)) {
//                return;
//            }
//
//
//            Item targetItem = (Item)hit; //todo
//
//            if(targetItem.fileHandle.isDirectory() && !targetItem.fileHandle.path().equals(fileHandle.path())) {
//
//                Object object = payload.getObject();
//                if (object instanceof Array) {
//                    Array<Item> array = (Array<Item>) object;
//                    Array<Item> copy = new Array<>();
//                    copy.addAll(array);
//                    for (Item sourceItem : copy) {
//                        if (!sourceItem.fileHandle.path().equals(targetItem.fileHandle.path())) {
//                            AssetImporter.moveFile(sourceItem.fileHandle, targetItem.fileHandle, false);
//                        }
//                    }
//                } else if (object instanceof GameAsset) {
//                    GameAsset sourceItem = (GameAsset) payload.getObject();
//                    FileHandle handle = sourceItem.getRootRawAsset().handle;
//                    if (!handle.path().equals(targetItem.fileHandle.path())) {
//                        AssetImporter.moveFile(handle, targetItem.fileHandle, false);
//                    }
//                } else if (object instanceof FileHandle) {
//                    FileHandle handle = (FileHandle) payload.getObject();
//                    if (!handle.path().equals(targetItem.fileHandle.path())) {
//                        AssetImporter.moveFile(handle, targetItem.fileHandle, false);
//                    }
//                }
//
//                for (Item item : selected) {
//                    item.deselect();
//                }
//                selected.clear();
//                reportSelectionChanged();
//            }
//        }
//    });
    }

    private void itemClicked (Item item, boolean rightClick) {
        boolean addToSelection = TalosInputProcessor.ctrlPressed();
        boolean shiftSelectRange = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);

        if (rightClick) {
            clearSelection();
            selected.add(item);
            item.select();
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

            float positionInParent = items.getHeight() - (found.getY() + found.getHeight()/2f);

            if (positionInParent < topY || positionInParent > (topY + scrollHeight)) {
                scrollPane.setScrollY(positionInParent - scrollHeight/2f);
            }

        }
    }

    /**
     * Compares two FileHandles and sorts them in alphabetical order based on their names. Gives priority to
     * directory if names are equal.
     */
    private static class DirectoryViewFileComparator implements Comparator<FileHandle> {

        @Override
        public int compare(FileHandle o1, FileHandle o2) {
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

            if(pathname.getName().endsWith(".tse")) return false;
            if(pathname.getName().equals(".DS_Store")) return false;
            if(pathname.getName().endsWith(".meta")) return false;
            if(pathname.getName().endsWith(".p")) return false;

            return true;
        }
    }

    public FileHandle getCurrentFolder () {
        return fileHandle;
    }

    public void registerTarget(DragAndDrop.Target externalTarget) {
        externalTargets.add(externalTarget);
        dragAndDrop.addTarget(externalTarget);
    }

    public void unregisterTarget(DragAndDrop.Target externalTarget) {
        externalTargets.removeValue(externalTarget, true);
        dragAndDrop.removeTarget(externalTarget);
    }


}
