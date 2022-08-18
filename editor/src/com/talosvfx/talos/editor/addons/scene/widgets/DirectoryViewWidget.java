package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.widgets.ui.ActorCloneable;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;
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

    public DirectoryViewWidget () {
        emptyFolderTable = new Table();
        Label emptyFolder = new Label("This folder is empty.", TalosMain.Instance().getSkin());
        emptyFolderTable.add(emptyFolder).expand().center().top().padTop(20);

        items = new ItemGroup();
        addListener(new ClickListener() {
            int selectionStart;

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
//                DirectoryWidgetTrash.ItemView selectCandidate = getSelectCandidate();
//                if(selectCandidate != null && SceneEditorWorkspace.isRenamePressed(keycode)) {
//                    for(DirectoryWidgetTrash.ItemView item: items) {
//                        if(item.fileHandle.path().equals(selectCandidate.fileHandle.path())) {
//                            // found it
//                            item.setToRename();
//                        }
//                    }
//                }
//
//                ProjectExplorerWidget projectExplorer = SceneEditorAddon.get().projectExplorer;
//
//                if(keycode == Input.Keys.X && TalosInputProcessor.ctrlPressed()) {
//                    projectExplorer.invokeCut(convertToFileArray(selected));
//                }
//
//                if(keycode == Input.Keys.C && TalosInputProcessor.ctrlPressed()) {
//                    projectExplorer.invokeCopy(convertToFileArray(selected));
//                }
//
//                if(keycode == Input.Keys.V && TalosInputProcessor.ctrlPressed()) {
//                    projectExplorer.invokePaste(getCurrentFolder());
//                }
//
//                if(keycode == Input.Keys.FORWARD_DEL) {
//                    Array<String> paths = new Array<>();
//                    for(DirectoryWidgetTrash.ItemView file: selected) {
//                        paths.add(file.fileHandle.path());
//                    }
//                    projectExplorer.deletePath(paths);
//                }
//
//                if(keycode == Input.Keys.A && TalosInputProcessor.ctrlPressed()) {
//                    selectAllFiles();
//                    reportSelectionChanged();
//                }
//
                return super.keyDown(event, keycode);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);

                if (button == 1) {
                    // logic for right click
                    // 1. if no item is under mouse, open context menu for current folder
                    // 2. else, if old selection contains item under mouse open context menu with current selection
                    //          else open context menu with only the item under mouse in it
                } else if (button == 0) {
                    // logic for left click
                    // Track last item selected not by shift-click. On shift-click, select all items between
                    // that item and the current click inclusive.
                    if (overItem >= 0) {
                        Item item = (Item) items.getChild(overItem);
                        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                            if (selected.contains(overItem)) {
                                item.deselect();
                                selected.removeValue(overItem);
                            } else {
                                item.select();
                                selected.add(overItem);
                            }
                            selectionStart = overItem;
                        } else if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                            int start = Math.min(selected.first(), overItem);
                            int end = Math.max(selected.peek(), overItem);
                            for (int i = start; i <= end; i++) {
                                Item toSelect = (Item) items.getChild(i);
                                toSelect.select();
                                selected.add(i);
                            }
                        } else {
                            for (int i : selected.items) {
                                Item selectedItem = (Item) items.getChild(i);
                                selectedItem.deselect();
                            }
                            selected.clear();
                            item.select();
                            selected.add(overItem);
                            selectionStart = overItem;
                        }
                    } else {
                        for (int i : selected.items) {
                            Item item = (Item) items.getChild(i);
                            item.deselect();
                        }
                        selected.clear();
                        selectionStart = -1;
                    }
                }
//                super.touchUp(event, x, y, pointer, button);
//
//                if(preventDeselect) {
//                    preventDeselect = false;
//                    return;
//                }
//
//                DirectoryWidgetTrash.ItemView itemViewAt = getFileAt(x, y);
//
//                if(button == 1) {
//                    if(itemViewAt != null) {
//                        if (!selected.contains(itemViewAt, true)) {
//                            selectFile(itemViewAt);
//                            reportSelectionChanged();
//                        }
//                        SceneEditorAddon.get().projectExplorer.showContextMenu(convertToFileArray(selected), true);
//                    } else {
//                        Array<FileHandle> list = new Array<>();
//                        list.add(fileHandle);
//                        SceneEditorAddon.get().projectExplorer.showContextMenu(list, true);
//                    }
//                } else if(button == 0) {
//                    float diff = TimeUtils.millis() - timeClicked;
//                    DirectoryWidgetTrash.ItemView fileToSelect = getFileAt(x, y);
//
//                    if(fileToSelect != null) {
//                        if(TalosInputProcessor.ctrlPressed()) {
//                            if(selected.contains(fileToSelect, true)) {
//                                removeFromSelection(fileToSelect);
//                            } else {
//                                addToSelection(fileToSelect);
//                            }
//                            reportSelectionChanged();
//                            selectionStart = items.indexOf(fileToSelect, true);
//                        } else {
//                            if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
//                                if(selectionStart >= 0) {
//                                    int aPoint = items.indexOf(fileToSelect, true);
//                                    int bPoint = selectionStart;
//                                    int min = Math.min(aPoint, bPoint);
//                                    int max = Math.max(aPoint, bPoint);
//                                    unselectFiles();
//                                    for(int i = min; i <= max; i++) {
//                                        addToSelection(items.get(i));
//                                    }
//                                    reportSelectionChanged();
//                                }
//                            } else {
//                                selectionStart = items.indexOf(fileToSelect, true);
//                                selectFile(fileToSelect);
//                                reportSelectionChanged();
//                            }
//                        }
//                    } else {
//                        selectionStart = -1;
//                        unselectFiles();
//                        reportSelectionChanged();
//                    }
//
//                    if(diff < 500 && prevPos.dst(x, y) < 40) {
//                        timeClicked = 0;
//                        prevPos.set(0, 0);
//                        DirectoryWidgetTrash.ItemView fileAt = getFileAt(x, y);
//                        if(fileAt != null) {
//                            AssetImporter.fileOpen(fileAt.fileHandle);
//                        } else {
//                            // go up
//                            if(fileHandle != null) {
//                                FileHandle parent = fileHandle.parent();
//                                FileHandle projectFolder = SceneEditorAddon.get().workspace.getProjectFolder();
//                                if(parent.path().startsWith(projectFolder.path())) {
//                                    SceneEditorAddon.get().projectExplorer.select(parent.path());
//                                }
//
//                            }
//                        }
//                    }
//
//                    timeClicked = TimeUtils.millis();
//                    prevPos.set(x, y);
//                }
//
//                if(getStage() != null) {
//                    getStage().setKeyboardFocus(DirectoryWidgetTrash.this);
//                }
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

    /**
     * Clears old items and populates the view with items in current directory.
     * Indicates if the folder is empty.
     * @param directory directory exists and it's directory.
     */
    private void fillItems (FileHandle directory) {
        items.clear();

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
            });
            item.setFile(fileHandle);
            items.addActor(item);
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
            return null;
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
    }
}
