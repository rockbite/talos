package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.SnapshotArray;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.widgets.ui.ActorCloneable;
import com.talosvfx.talos.editor.widgets.ui.ContextualMenu;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

public class DirectoryViewWidget extends Table {
    private static final DirectoryViewFileComparator DIRECTORY_VIEW_FILE_COMPARATOR = new DirectoryViewFileComparator();
    private static final FileFilter DIRECTORY_VIEW_FILE_FILTER = new DirectoryViewFileFilter();

    private Array<Item> selected = new Array<>();
    private Item overItem;

    private ItemGroup items;
    private Table emptyFolderTable;

    private ContextualMenu contextualMenu;

    public DirectoryViewWidget () {
        emptyFolderTable = new Table();
        Label emptyFolder = new Label("This folder is empty.", TalosMain.Instance().getSkin());
        emptyFolderTable.add(emptyFolder).expand().center().top().padTop(20);

        items = new ItemGroup();
        addListener(new ClickListener() {

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                Item underMouse = getItemAt(x, y);
                if (underMouse != null) {
                    overItem = underMouse;
                } else {
                    overItem = null;
                }
                return true;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                Item underMouse = getItemAt(x, y);
                if(button == 1) {
                    if (underMouse != null) {
                        if (!selected.contains(underMouse, true)) {
                            selected.clear();
                            selected.add(underMouse);
//                            reportSelectionChanged();
                        }
                        SceneEditorAddon.get().projectExplorer.showContextMenu(convertToFileArray(selected), true);
                    } else {
                        SceneEditorAddon.get().projectExplorer.showContextMenu(convertToFileArray(selected), true);
                    }
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                Item underMouse = getItemAt(x, y);
                if (underMouse == null) {
                    for (Item item : selected) {
                        item.deselect();
                    }
                    selected.clear();
                    return;
                }
//                if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
//                    for (Item selected : selected) {
//                        selected.deselect();
//                    }
//                    selected.clear();
//                }
//                underMouse.select();
//                selected.add(underMouse);
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

    private Array<FileHandle> convertToFileArray (Array<Item> selected) {
        Array<FileHandle> handles = new Array<>();
        for (Item item : selected) {
            handles.add(item.fileHandle);
        }
        return handles;
    }

    private Vector2 tmpGetItemAt = new Vector2();
    private Item getItemAt (float x, float y) {
        Vector2 tmp = tmpGetItemAt;
        for(Actor item: items.getChildren()) {
            tmp.set(x, y);
            localToStageCoordinates(tmp);
            item.stageToLocalCoordinates(tmp);

            if (item.hit(tmp.x, tmp.y, false) != null) {
                return (Item) item;
            }
        }
        return null;
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

        public Item () {
            Skin skin = TalosMain.Instance().getSkin();
            icon = new Image(null, Scaling.fit);
            brokenStatus = new Image(TalosMain.Instance().getSkin().getDrawable("ic-fileset-file"));
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
            } else if (overItem == this) {
                Drawable bg = ColorLibrary.obtainBackground(TalosMain.Instance().getSkin(), "white", ColorLibrary.BackgroundColor.LIGHT_GRAY);
                bg.draw(batch, getX(), getY(), getWidth(), getHeight());
            }

            float w = getWidth(), h = getHeight();

            float lw = w, lh = label.getLabel().getHeight();
            float lpadbot = 0.04f * getHeight();
            float lx = getX(), ly = getY() + lpadbot;
            label.getLabel().setWidth(lw);
            label.setPosition(lx, ly);
            label.draw(batch, parentAlpha);

            float ipadBot = 0.1f * getHeight();
            float iw = (3.0f / 5.0f) * w, ih = (3.0f / 5.0f) * h - lpadbot - ipadBot;
            float ix = getX() - iw / 2f + w / 2f;
            float iy = Math.max(getY() + lh, getY() - ih / 2f + h / 2f) + ipadBot; // val1 - above label, val2 - in the center of item
            icon.setSize(iw, ih);
            icon.setPosition(ix, iy);
            icon.draw(batch, parentAlpha);

            if (gameAsset != null && gameAsset.isBroken()) {
                float bsix = getX(), bsiy = iy;
                brokenStatus.setPosition(bsix, bsiy);
                brokenStatus.draw(batch, parentAlpha);
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
    }
}
