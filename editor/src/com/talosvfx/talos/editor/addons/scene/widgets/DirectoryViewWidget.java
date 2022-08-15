package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.SnapshotArray;
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

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

public class DirectoryViewWidget extends Table {
    private static final DirectoryViewFileComparator DIRECTORY_VIEW_FILE_COMPARATOR = new DirectoryViewFileComparator();
    private static final FileFilter DIRECTORY_VIEW_FILE_FILTER = new DirectoryViewFileFilter();

    private ItemGroup items;

    public DirectoryViewWidget () {
        items = new ItemGroup();
        items.pad(20);
        items.space(5);
        items.wrapSpace(5);

        add(items).grow();
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
        Arrays.sort(content, DIRECTORY_VIEW_FILE_COMPARATOR);

        for (FileHandle fileHandle : content) {
            if (!DIRECTORY_VIEW_FILE_FILTER.accept(fileHandle.file())) {
                continue; // skip over unwanted files
            }
            Item item = new Item();
            item.setFile(fileHandle);
            item.debug();
            items.addActor(item);
        }
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
    }

    /**
     * Compares two FileHandles and sorts them in alphabetical order based on their names. Gives priority to
     * directory if names are equal.
     */
    private static class DirectoryViewFileComparator implements Comparator<FileHandle> {

        @Override
        public int compare(FileHandle o1, FileHandle o2) {
            String name1 = o1.nameWithoutExtension();
            String name2 = o2.nameWithoutExtension();

            boolean isDirectory1 = o1.isDirectory();
            boolean isDirectory2 = o2.isDirectory();
            if (name1.compareTo(name2) > 0) { // name1, name2
                return 1;
            } else if (name1.compareTo(name2) < 0) { // name2, name1
                return -1;
            } else if (isDirectory1 && !isDirectory2) { // dir, file
                return 1;
            } else if (!isDirectory1 && isDirectory2) { // file, dir
                return -1;
            }
            return 0;
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

    private static class ItemGroup extends WidgetGroup {
        private float prefWidth, prefHeight, lastPrefHeight;
        private boolean sizeInvalid = true;
        private FloatArray rowSizes; // row width, row height, ...

        private boolean round = true;
        private float space, wrapSpace, padTop, padLeft, padBottom, padRight;

        public ItemGroup () {
            setTouchable(Touchable.childrenOnly);
        }

        @Override
        public void invalidate() {
            super.invalidate();
            sizeInvalid = true;
        }

        private void computeSize () {
            sizeInvalid = false;
            SnapshotArray<Actor> children = getChildren();
            int n = children.size;
            prefHeight = 0;
            prefWidth = 0;
            if (rowSizes == null)
                rowSizes = new FloatArray();
            else
                rowSizes.clear();
            FloatArray rowSizes = this.rowSizes;
            float space = this.space, wrapSpace = this.wrapSpace;
            float pad = padLeft + padRight, groupWidth = getWidth() - pad, x = 0, y = 0, rowHeight = 0;
            int i = 0, incr = 1;
            for (; i != n; i += incr) {
                Actor child = children.get(i);

                float width, height;
                if (child instanceof Layout) {
                    Layout layout = (Layout)child;
                    width = layout.getPrefWidth();
                    if (width > groupWidth) width = Math.max(groupWidth, layout.getMinWidth());
                    height = layout.getPrefHeight();
                } else {
                    width = child.getWidth();
                    height = child.getHeight();
                }

                float incrX = width + (x > 0 ? space : 0);
                if (x + incrX > groupWidth && x > 0) {
                    rowSizes.add(x);
                    rowSizes.add(rowHeight);
                    prefWidth = Math.max(prefWidth, x + pad);
                    if (y > 0) y += wrapSpace;
                    y += rowHeight;
                    rowHeight = 0;
                    x = 0;
                    incrX = width;
                }
                x += incrX;
                rowHeight = Math.max(rowHeight, height);
            }
            rowSizes.add(x);
            rowSizes.add(rowHeight);
            prefWidth = Math.max(prefWidth, x + pad);
            if (y > 0) y += wrapSpace;
            prefHeight = Math.max(prefHeight, y + rowHeight);
            prefHeight += padTop + padBottom;
            if (round) {
                prefWidth = Math.round(prefWidth);
                prefHeight = Math.round(prefHeight);
            }
            if (hasParent()) {
                prefWidth = Math.max(getParent().getWidth(), prefWidth);
            }
        }

        public void layout () {
            if (sizeInvalid) computeSize();

            float prefHeight = getPrefHeight();
            if (prefHeight != lastPrefHeight) {
                lastPrefHeight = prefHeight;
                invalidateHierarchy();
            }

            boolean round = this.round;
            float space = this.space, wrapSpace = this.wrapSpace;
            float rowY = prefHeight - padTop, groupWidth = getWidth(), xStart = padLeft, x = 0, rowHeight = 0, rowDir = -1;

            rowY += getHeight() - prefHeight; // align top

            xStart += (groupWidth - prefWidth) / 2; // align the whole group to center

            groupWidth -= padRight;

            FloatArray rowSizes = this.rowSizes;
            SnapshotArray<Actor> children = getChildren();
            int i = 0, n = children.size, incr = 1;
            for (int r = 0; i != n; i += incr) {
                Actor child = children.get(i);

                float width, height;
                Layout layout = null;
                if (child instanceof Layout) {
                    layout = (Layout)child;
                    width = layout.getPrefWidth();
                    if (width > groupWidth) width = Math.max(groupWidth, layout.getMinWidth());
                    height = layout.getPrefHeight();
                } else {
                    width = child.getWidth();
                    height = child.getHeight();
                }

                if (x + width > groupWidth || r == 0) {
                    r = Math.min(r, rowSizes.size - 2); // In case an actor changed size without invalidating this layout.
                    x = xStart;
                    rowHeight = rowSizes.get(r + 1);
                    if (r > 0) rowY += wrapSpace * rowDir;
                    rowY += rowHeight * rowDir;
                    r += 2;
                }

                if (layout != null) {
                    height = Math.max(height, layout.getMinHeight());
                    float maxHeight = layout.getMaxHeight();
                    if (maxHeight > 0 && height > maxHeight) height = maxHeight;
                }

                float y = rowY;
                y += rowHeight - height; // align top

                if (round)
                    child.setBounds(Math.round(x), Math.round(y), Math.round(width), Math.round(height));
                else
                    child.setBounds(x, y, width, height);
                x += width + space;

                if (layout != null) layout.validate();
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
    }

    private static class Item extends Widget implements ActorCloneable<Item> {
        private Image icon;
        private Image brokenStatus;
        private EditableLabel label;

        private FileHandle fileHandle;
        private GameAsset<?> gameAsset;

        private GameObject basicGameObject;

        public Item () {
            Skin skin = TalosMain.Instance().getSkin();
            icon = new Image();
            brokenStatus = new Image(TalosMain.Instance().getSkin().getDrawable("ic-fileset-file"));
            label = new EditableLabel("text", skin);

            setTouchable(Touchable.enabled);
        }

        private void setFile (FileHandle fileHandle) {
            String name = fileHandle.name();
            label.setText(name);
            label.getLabel().setWrap(true);
            label.getLabel().setEllipsis(true);

            if (fileHandle.isDirectory()) {
                icon.setDrawable(TalosMain.Instance().getSkin().getDrawable("ic-folder-big"));
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
            icon.setSize(100, 100);
            icon.setPosition(getX() - 50 + 125/2f, getY() - 50 + 125/2f);
            icon.draw(batch, parentAlpha);
        }

        @Override
        public Item copyActor(Item copyFrom) {
            return null;
        }

        @Override
        public float getPrefWidth() {
            return 125;
        }

        @Override
        public float getPrefHeight() {
            return 125;
        }
    }
}
