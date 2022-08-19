package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.TalosInputProcessor;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.RawAsset;
import com.talosvfx.talos.editor.addons.scene.events.PropertyHolderSelected;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.logic.MultiPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.widgets.ui.ActorCloneable;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

public class DirectoryViewWidget extends Table {
    private static final DirectoryViewFileComparator DIRECTORY_VIEW_FILE_COMPARATOR = new DirectoryViewFileComparator();
    private static final FileFilter DIRECTORY_VIEW_FILE_FILTER = new DirectoryViewFileFilter();

    private IntArray selected = new IntArray();
    private int overItem = -1;

    private ItemGroup items;
    private Table emptyFolderTable;

    private FileHandle fileHandle;

    private DragAndDrop dragAndDrop;
    private Array<DragAndDrop.Target> externalTargets;

    private boolean preventDeselect = false;

    public DirectoryViewWidget () {
        emptyFolderTable = new Table();
        Label emptyFolder = new Label("This folder is empty.", TalosMain.Instance().getSkin());
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
                if (selected.size == 1) {
                    int i = selected.first();
                    Item item = (Item) items.getChild(i);
                    item.rename();
                }

                ProjectExplorerWidget projectExplorer = SceneEditorAddon.get().projectExplorer;

                if (keycode == Input.Keys.X && TalosInputProcessor.ctrlPressed()) {
                    projectExplorer.invokeCut(convertToFileArray(selected));
                }

                if (keycode == Input.Keys.C && TalosInputProcessor.ctrlPressed()) {
                    projectExplorer.invokeCopy(convertToFileArray(selected));
                }

                if (keycode == Input.Keys.V && TalosInputProcessor.ctrlPressed()) {
                    projectExplorer.invokePaste(fileHandle);
                }

                if (keycode == Input.Keys.FORWARD_DEL) {
                    Array<String> paths = new Array<>();
                    for (int i = 0; i < selected.size; i++) {
                        int entry = selected.get(i);
                        Item item = (Item) items.getChild(entry);
                        paths.add(item.getFileHandle().path());
                    }
                    projectExplorer.deletePath(paths);
                }

                if (keycode == Input.Keys.A && TalosInputProcessor.ctrlPressed()) {
                    for (Actor child : items.getChildren()) {
                        Item item = (Item) child;
                        int i = items.getChildren().indexOf(item, true);
                        if (!selected.contains(i)) {
                            item.select();
                            selected.add(i);
                        }
                    }
                    reportSelectionChanged();
                }

                return true;
            }
        });

        addListener(new ClickListener() {
            int selectionStart;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                ProjectExplorerWidget projectExplorer = SceneEditorAddon.get().projectExplorer;
                // TODO: make it, so if you click anywhere outside of this place it will clean up all selection
                if (overItem == -1) {
                    if (button == 0) {
                        for (int i : selected.items) {
                            if (selected.contains(i)) {
                                Item item = (Item) items.getChild(i);
                                item.deselect();
                            }
                        }
                        selected.clear();
                        selectionStart = -1;
                        reportSelectionChanged();
                    }  else {
                        Array<FileHandle> current = new Array<>(1);
                        current.add(fileHandle);
                        projectExplorer.showContextMenu(current, true);
                    }
                } else {
                    if (button == 1) {
                        if (!selected.contains(overItem)) {
                            for (int i = 0; i < selected.size; i++) {
                                int entry = selected.get(i);
                                Item item = (Item) items.getChild(entry);
                                item.deselect();
                            }
                            selected.clear();
                            Item item = (Item) items.getChild(overItem);
                            item.select();
                            selected.add(overItem);
                            reportSelectionChanged();
                        }
                        Array<FileHandle> selection = convertToFileArray(selected);
                        projectExplorer.showContextMenu(selection, true);
                    } else if (button == 0) {
                        if (overItem >= 0) {
                            Item item = (Item) items.getChild(overItem);
                            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                                if (selected.contains(overItem)) {
                                    item.deselect();
                                    selected.removeValue(overItem);
                                } else {
                                    item.select();
                                    selected.add(overItem);
                                    selectionStart = overItem;
                                }
                                reportSelectionChanged();
                            } else if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && selectionStart >= 0) {
                                int start = Math.min(selectionStart, overItem);
                                int end = Math.max(selectionStart, overItem);
                                for (int i = start; i <= end; i++) {
                                    if (!selected.contains(i)) {
                                        Item toSelect = (Item) items.getChild(i);
                                        toSelect.select();
                                        selected.add(i);
                                    }
                                }
                                selectionStart = overItem;
                                reportSelectionChanged();
                            } else {
                                for (int i : selected.items) {
                                    if (selected.contains(i)) {
                                        Item selectedItem = (Item) items.getChild(i);
                                        selectedItem.deselect();
                                    }
                                }
                                selected.clear();
                                item.select();
                                selected.add(overItem);
                                selectionStart = overItem;
                                reportSelectionChanged();
                            }
                        }
                    }
                }
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if(preventDeselect) {
                    preventDeselect = false;
                    return;
                }
            }
        });

        items.setCellSize(50);
        items.pad(20);
        items.wrapSpace(10);
        items.space(10);
        ScrollPane scrollPane = new ScrollPane(items);
        scrollPane.setScrollbarsVisible(true);
        Stack stack = new Stack(scrollPane, emptyFolderTable);
        add(stack).grow().height(0).row();
        Slider slider = new Slider(50, 125, 1, false, TalosMain.Instance().getSkin());
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                items.setCellSize(slider.getValue());
                items.invalidateHierarchy();
            }
        });
        add(slider).width(125).pad(5, 10, 5, 10).expandX().right();
    }

    private void reportSelectionChanged() {
        if(!selected.isEmpty()) {
            IPropertyHolder holder = null;
            if (selected.size == 1) {
                int i = selected.first();
                Item item = (Item) items.getChild(i);
                if (item.gameAsset != null) {
                    if (!item.gameAsset.isBroken()) {
                        holder = item.gameAsset.getRootRawAsset().metaData;
                    }
                }
            } else if (selected.size > 1) {
                ObjectSet<AMetadata> list = new ObjectSet<AMetadata>();
                for (int i = 0; i < selected.size; i ++) {
                    int entry = selected.get(i);
                    Item item = (Item) items.getChild(entry);
                    if (item.gameAsset != null) {
                        if (!item.gameAsset.isBroken()) {
                            RawAsset rootRawAsset = item.gameAsset.getRootRawAsset();
                            list.add(rootRawAsset.metaData);
                        }
                    }
                }
                holder = new MultiPropertyHolder(list);
            }

            if(holder != null) {
                Notifications.fireEvent(Notifications.obtainEvent(PropertyHolderSelected.class).setTarget(holder));
            }
        }
    }

    public void rename () {
        if (selected.size == 1) {
            int i = selected.first();
            Item item = (Item) items.getChild(i);
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

    private Array<FileHandle> convertToFileArray (IntArray selected) {
        Array<FileHandle> handles = new Array<>();
        for (int i = 0; i < selected.size; i++) {
            int entry = selected.get(i);
            Item item = (Item) items.getChild(entry);
            handles.add(item.getFileHandle());
        }

        return handles;
    }

    /**
     * Clears old items and populates the view with items in current directory.
     * Indicates if the folder is empty.
     * @param directory directory exists and it's directory.
     */
    private void fillItems (FileHandle directory) {
        // reset state
        overItem = -1;
        selected.clear();
        items.clear();
        dragAndDrop.clear();
        for (DragAndDrop.Target externalTarget : externalTargets) {
            dragAndDrop.addTarget(externalTarget);
        }

        dragAndDrop.addTarget(new DragAndDrop.Target(SceneEditorAddon.get().workspace) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {

                Actor actor = getActor();

                return true;
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                Object object = payload.getObject();
                if(object instanceof Array) {

                    Array<DirectoryWidgetTrash.ItemView> array = (Array<DirectoryWidgetTrash.ItemView>) object;

                    for (int i = 0; i < selected.size; i++) {
                        int entry = selected.get(i);
                        Item item = (Item) items.getChild(entry);
                        if (item.gameAsset != null) {
                            AssetImporter.createAssetInstance(item.gameAsset, SceneEditorAddon.get().workspace.getRootGO());
                        }
                    }
                } else {
                    if (object instanceof GameAsset) {
                        AssetImporter.createAssetInstance((GameAsset) payload.getObject(), SceneEditorAddon.get().workspace.getRootGO());
                    }
                    if (object instanceof FileHandle) {
                        //Do nothing here anymore
                    }
                }

                for (int i : selected.items) {
                    if (selected.contains(i)) {
                        Item item = (Item) items.getChild(i);
                        item.deselect();
                    }
                }
                selected.clear();
                reportSelectionChanged();
            }
        });

        dragAndDrop.addTarget(new DragAndDrop.Target(SceneEditorAddon.get().hierarchy) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                return true;
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                Actor targetActor = getActor();
            }
        });

        for (ObjectMap.Entry<String, FilteredTree.Node<String>> node : SceneEditorAddon.get().projectExplorer.getNodes()) {
            dragAndDrop.addTarget(new DragAndDrop.Target(node.value.getActor()) {
                @Override
                public boolean drag (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                    return true;
                }

                @Override
                public void drop (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {

                    // Payload is either an Array<ItemView>, Or its a FileHandle, Or its a GameAsset

                    fileHandle = ((ProjectExplorerWidget.RowWidget) getActor()).getFileHandle();

                    Object object = payload.getObject();
                    if (object instanceof Array) {
                        Array<Item> array = (Array<Item>) object;
                        for (Item sourceItem : array) {
                            if (!sourceItem.fileHandle.path().equals(fileHandle.path())) {
                                AssetImporter.moveFile(sourceItem.fileHandle, fileHandle, false);
                            }
                        }
                    } else if (object instanceof GameAsset) {
                        GameAsset<?> sourceItem = (GameAsset) payload.getObject();
                        FileHandle handle = sourceItem.getRootRawAsset().handle;
                        if (!handle.path().equals(fileHandle.path())) {
                            AssetImporter.moveFile(handle, fileHandle, false);
                        }
                    } else if (object instanceof FileHandle) {
                        FileHandle handle = (FileHandle) payload.getObject();
                        if (!handle.path().equals(fileHandle.path())) {
                            AssetImporter.moveFile(handle, fileHandle, false);
                        }
                    }
                }
            });
        }

        dragAndDrop.addTarget(new DragAndDrop.Target(this) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {

                if(overItem >= 0) {
                    return true;
                }

                return false;
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                if (overItem == -1) {
                    return;
                }

                Item targetItem = (Item) items.getChild(overItem);

                if(targetItem.fileHandle.isDirectory() && !targetItem.fileHandle.path().equals(fileHandle.path())) {

                    Object object = payload.getObject();
                    if (object instanceof Array) {
                        Array<Item> array = (Array<Item>) object;
                        for (Item sourceItem : array) {
                            if (!sourceItem.fileHandle.path().equals(targetItem.fileHandle.path())) {
                                AssetImporter.moveFile(sourceItem.fileHandle, targetItem.fileHandle, false);
                            }
                        }
                    } else if (object instanceof GameAsset) {
                        GameAsset sourceItem = (GameAsset) payload.getObject();
                        FileHandle handle = sourceItem.getRootRawAsset().handle;
                        if (!handle.path().equals(targetItem.fileHandle.path())) {
                            AssetImporter.moveFile(handle, targetItem.fileHandle, false);
                        }
                    } else if (object instanceof FileHandle) {
                        FileHandle handle = (FileHandle) payload.getObject();
                        if (!handle.path().equals(targetItem.fileHandle.path())) {
                            AssetImporter.moveFile(handle, targetItem.fileHandle, false);
                        }
                    }

                    for (int i : selected.items) {
                        if (selected.contains(i)) {
                            Item item = (Item) items.getChild(i);
                            item.deselect();
                        }
                    }
                    selected.clear();
                    reportSelectionChanged();
                }
            }
        });

        FileHandle[] content = directory.list();
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
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    overItem = items.getChildren().indexOf(item, true);
                    item.setMouseover(true);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    overItem = -1;
                    item.setMouseover(false);
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (getTapCount() == 2) {
                        for (int i : selected.items) {
                            if (selected.contains(i)) {
                                Item to = (Item) items.getChild(i);
                                to.deselect();
                            }
                        }
                        selected.clear();
                        selected.add(items.getChildren().indexOf(item, true));
                        item.select();
                        AssetImporter.fileOpen(item.getFileHandle());
                    }
                }
            });
            item.setFile(fileHandle);
            items.addActor(item);

            dragAndDrop.addSource(new DragAndDrop.Source(item) {
                @Override
                public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                    DragAndDrop.Payload payload = new DragAndDrop.Payload();

                    preventDeselect = true;

                    int i = items.getChildren().indexOf(item, true);
                    if(!selected.contains(i)) {
                        selected.clear();
                        selected.add(i);
                    }

                    if(selected.size == 1) {
                        Item newView = new Item();
                        Actor dragging = ((ActorCloneable) newView).copyActor(item);
                        payload.setDragActor(dragging);
                        if (newView.gameAsset != null) {
                            payload.setObject(newView.gameAsset);
                        } else {
                            payload.setObject(fileHandle);
                        }
                    } else {
                        Label dragging = new Label("Multiple selection", TalosMain.Instance().getSkin());
                        payload.setDragActor(dragging);
                        payload.setObject(selected);
                    }

                    return payload;
                }
            });
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

    /**
     * Completely ignores the pref size of its children.
     */
    private static class ItemGroup extends WidgetGroup {
        private float prefHeight;
        private float cellWidth, cellHeight;

        private float space, wrapSpace, padTop, padLeft, padBottom, padRight;

        public ItemGroup () {
            setTouchable(Touchable.childrenOnly);
        }

        @Override
        public void invalidate() {
            super.invalidate();
        }

        public void layout () {
            float padLeft = this.padLeft, padRight = this.padRight, pad = padLeft + padRight, space = this.space, wrapSpace = this.wrapSpace;
            float width = cellWidth, height = cellHeight;
            float maxWidth = getWidth() - pad;
            SnapshotArray<Actor> children = getChildren();
            int n = children.size;
            if (n < 1) {
                return;
            }

            if (n * width + (n - 1) * space <= maxWidth) { // strategy 1 - align.topLeft and fixed space
                float rowHeight = height, x = padLeft;

                float startY = getHeight() - padTop - rowHeight;
                int i = 0, incr = 1;
                for (; i != n; i += incr) {
                    Actor child = children.get(i);
                    Layout layout = null;
                    if (child instanceof Layout) {
                        layout = (Layout) child;
                    }
                    float y = startY;
                    y += rowHeight - height;
                    child.setBounds(x, y, width, height);
                    x += width + space;
                    if (layout != null) layout.validate();
                }
                prefHeight = padTop + padBottom + cellHeight;
            } else {
                float rowY = getHeight() - padTop, groupWidth = getWidth(), xStart = padLeft, x = 0, rowHeight = 0, rowDir = -1;

                int canFit = (int) ((maxWidth - space) / (width + space));
                if (canFit > 1 && n > 1) { // strategy 2 - align.topCenter and dynamic space between
                    maxWidth += pad;
                    space = (maxWidth - canFit * width) / (canFit + 1);
                    xStart = space;

                    int i = 0, incr = 1;
                    for (int r = 0; i != n; i += incr) {
                        Actor child = children.get(i);

                        Layout layout = null;
                        if (child instanceof Layout) {
                            layout = (Layout)child;
                        }

                        if ((i % canFit) == 0 || r == 0) {
                            x = xStart;
                            rowHeight = height;
                            if (r > 0) rowY += wrapSpace * rowDir;
                            rowY += rowHeight * rowDir;
                            r += 1;
                        }

                        float y = rowY;
                        y += rowHeight - height;

                        child.setBounds(x, y, width, height);
                        prefHeight = r * height + (r - 1) * wrapSpace + padBottom + padTop;

                        x += width + space;

                        if (layout != null) layout.validate();
                    }
                } else { // strategy 3 - one item per row
                    int i = 0, incr = 1;
                    for (int r = 0; i != n; i += incr) {
                        Actor child = children.get(r);

                        Layout layout = null;
                        if (child instanceof Layout) {
                            layout = (Layout)child;
                        }

                        x = xStart;
                        x += (maxWidth - width) / 2;
                        rowHeight = height;
                        if (r > 0) rowY += wrapSpace * rowDir;
                        rowY += rowHeight * rowDir;
                        r += 1;

                        float y = rowY;
                        y += rowHeight - height;

                        child.setBounds(x, y, width, height);

                        if (layout != null) layout.validate();
                    }

                    prefHeight = padTop + padBottom + n * cellHeight + (n - 1) * wrapSpace;
                }
            }
        }

        /** Sets the padTop, padLeft, padBottom, and padRight to the specified value. */
        public ItemGroup pad (float pad) {
            padTop = pad;
            padLeft = pad;
            padBottom = pad;
            padRight = pad;
            return this;
        }

        /** Sets the horizontal space between children. */
        public ItemGroup space (float space) {
            this.space = space;
            return this;
        }

        public void setCellWidth (float width) {
            this.cellWidth = width;
        }

        public void setCellHeight (float height) {
            this.cellHeight = height;
        }

        public void setCellSize (float size) {
            setCellSize(size, size);
        }

        public void setCellSize (float width, float height) {
            setCellWidth(width);
            setCellHeight(height);
        }

        /** Sets the vertical space between rows when wrap is enabled. */
        public ItemGroup wrapSpace (float wrapSpace) {
            this.wrapSpace = wrapSpace;
            return this;
        }

        protected void drawDebugBounds (ShapeRenderer shapes) {
            super.drawDebugBounds(shapes);
            if (!getDebug()) return;
            shapes.set(ShapeRenderer.ShapeType.Line);
            if (getStage() != null) shapes.setColor(getStage().getDebugColor());
            shapes.rect(getX() + padLeft, getY() + padBottom, getOriginX(), getOriginY(), getWidth() - padLeft - padRight,
                    getHeight() - padBottom - padTop, getScaleX(), getScaleY(), getRotation());
        }

        @Override
        public float getMinWidth() {
            return padLeft + cellWidth + padRight;
        }

        @Override
        public float getPrefHeight() {
            return prefHeight;
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

    private class Item extends Widget implements ActorCloneable<Item> {
        private Image icon;
        private Image brokenStatus;
        private EditableLabel label;

        private FileHandle fileHandle;
        private GameAsset<?> gameAsset;

        private GameObject basicGameObject;

        private boolean selected;
        private boolean mouseover;

        private float padTop = 5, padTopSmall = 2, padTopBig = 7;
        private float padLeft = 5, padLeftSmall = 2, padLeftBig = 7;
        private float padBottom = 5, padBottomSmall = 2, padBottomBig = 7;
        private float padRight = 5, padRightSmall = 2, padRightBig = 7;
        private float space = 5, spaceSmall = 2, spaceBig = 7;

        public Item () {
            Skin skin = TalosMain.Instance().getSkin();
            icon = new Image(null, Scaling.fit, Align.center);
            brokenStatus = new Image(TalosMain.Instance().getSkin().newDrawable("ic-fileset-file"));
            brokenStatus.setColor(Color.RED);
            label = new EditableLabel("text", skin);
            label.getLabel().setAlignment(Align.center);
            setTouchable(Touchable.enabled);
        }

        private void setFile (FileHandle fileHandle) {
            String name = fileHandle.name();
            label.setText(name);
            label.getLabel().setWrap(true);
            label.getLabel().setEllipsis(true);

            if (fileHandle.isDirectory()) {
                FileHandle[] content = fileHandle.list();
                if (content.length == 0) {
                    icon.setDrawable(TalosMain.Instance().getSkin().getDrawable("ic-folder-big-empty"));
                } else {
                    icon.setDrawable(TalosMain.Instance().getSkin().getDrawable("ic-folder-big"));
                }
            } else {
                icon.setDrawable(TalosMain.Instance().getSkin().getDrawable("ic-file-big"));
                String extension = fileHandle.extension();
                if (extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg")) {
                    Texture texture = new Texture(fileHandle);
                    TextureRegionDrawable drawable = new TextureRegionDrawable(texture);
                    icon.setDrawable(drawable);
                    icon.setScaling(Scaling.fit);
                }
            }

            this.fileHandle = fileHandle;

            GameAsset<?> assetForPath = AssetRepository.getInstance().getAssetForPath(fileHandle, true);
            if (assetForPath != null) {
                gameAsset = assetForPath;
            }

            if (assetForPath != null) {
                GameObject parent = new GameObject();
                parent.addComponent(new TransformComponent());
                basicGameObject = parent;

                AssetImporter.fromDirectoryView = true; //tom is very naughty dont be like tom
                boolean success = AssetImporter.createAssetInstance(assetForPath, parent) != null;
                if (parent.getGameObjects() == null || parent.getGameObjects().size == 0) {
                    success = false;
                }
                AssetImporter.fromDirectoryView = false;

                if (success) {
                    //Game asset is legit, lets try to make one
                    GameObject copy = new GameObject();
                    copy.addComponent(new TransformComponent());

                    AssetImporter.fromDirectoryView = true; //tom is very naughty dont be like tom
                    AssetImporter.createAssetInstance(assetForPath, copy);
                    AssetImporter.fromDirectoryView = false;

                    MainRenderer uiSceneRenderer = SceneEditorWorkspace.getInstance().getUISceneRenderer();
                    Stage stage = SceneEditorWorkspace.getInstance().getStage();
                    uiSceneRenderer.setCamera((OrthographicCamera)stage.getCamera());
                    GameObjectActor gameObjectActor = new GameObjectActor(uiSceneRenderer, basicGameObject, copy, true);
                    gameObjectActor.setFillParent(true);
                }
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (selected) {
                Drawable bg = ColorLibrary.obtainBackground(TalosMain.Instance().getSkin(), "white", ColorLibrary.BackgroundColor.DARK_GRAY);
                bg.draw(batch, getX(), getY(), getWidth(), getHeight());
            } else if (mouseover) {
                Drawable bg = ColorLibrary.obtainBackground(TalosMain.Instance().getSkin(), "white", ColorLibrary.BackgroundColor.LIGHT_GRAY);
                bg.draw(batch, getX(), getY(), getWidth(), getHeight());
            }

            float w = getWidth(), h = getHeight();
            float padTop, padLeft, padBottom, padRight, space;
            if (w < 75) {
                padTop = padTopSmall;
                padLeft = padLeftSmall;
                padBottom = padBottomSmall;
                padRight = padRightSmall;
                space = spaceSmall;
            } else if (w >= 75) {
                padTop = this.padTop;
                padLeft = this.padLeft;
                padBottom = this.padBottom;
                padRight = this.padRight;
                space = this.space;
            } else {
                padTop = padTopBig;
                padLeft = padLeftBig;
                padBottom = padBottomBig;
                padRight = padRightBig;
                space = spaceBig;
            }

            w -= padLeft + padRight;
            h -= padTop + padBottom;

            float lw = w, lh = label.getLabel().getHeight();
            float lx = getX() + padLeft, ly = getY() + padBottom;
            label.getLabel().setWidth(lw);
            label.getTextField().setWidth(lw);
            label.setPosition(lx, ly);
            label.draw(batch, parentAlpha);

            float iw = w, ih = h - lh - space - padTop;
            float ix = getX() + padLeft;
            float iy = ly + lh + space;
            icon.setSize(iw, ih);
            icon.setPosition(ix, iy);
            icon.draw(batch, parentAlpha);

            if (gameAsset != null && gameAsset.isBroken()) {
                float bsix = getX() + padLeft, bsiy = iy;
                brokenStatus.setPosition(bsix, bsiy);
                brokenStatus.draw(batch, parentAlpha);
                batch.setColor(Color.WHITE);
            }
        }

        @Override
        public Item copyActor(Item copyFrom) {
            setFile(copyFrom.fileHandle);
            return this;
        }

        public boolean isSelected () {
            return selected;
        }

        public void select () {
            selected = true;
        }

        public void deselect () {
            selected = false;
        }

        public void setMouseover(boolean over) {
            mouseover = over;
        }

        public FileHandle getFileHandle() {
            return fileHandle;
        }

        public void rename() {
            label.setStage(getStage());
            label.setEditMode();
        }
    }
}
