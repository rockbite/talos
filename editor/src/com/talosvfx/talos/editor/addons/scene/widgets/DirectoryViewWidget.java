package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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
import org.w3c.dom.ls.LSOutput;

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
        items.setCellSize(125);
        items.pad(20);
        items.wrapSpace(10);
        items.space(10);

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

    /**
     * Completely ignores the pref size of its children.
     */
    private static class ItemGroup extends WidgetGroup {
        private boolean sizeInvalid = true;
        private float cellWidth, cellHeight;

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
            return;
//            SnapshotArray<Actor> children = getChildren();
//            int n = children.size;
//            prefHeight = 0;
//            prefWidth = 0;
//            if (rowSizes == null)
//                rowSizes = new FloatArray();
//            else
//                rowSizes.clear();
//            FloatArray rowSizes = this.rowSizes;
//            float wrapSpace = this.wrapSpace; //space = this.space,
//            float pad = padLeft + padRight, groupWidth = getWidth() - pad, x = 0, y = 0, rowHeight = 0;
//
//            float width = cellWidth, height = cellHeight;
//
//            float space = 0;
//            if (n > 2) {
//                int canFit = (int) (groupWidth / width);
//                space = (groupWidth - canFit * width) / (canFit - 1);
//            }
//
//            int i = 0, incr = 1;
//            for (; i != n; i += incr) {
////                Actor child = children.get(i);
////                child.setSize(width, height);
////                float width, height;
////                if (child instanceof Layout) {
////                    Layout layout = (Layout)child;
////                    width = layout.getPrefWidth();
////                    if (width > groupWidth) width = Math.max(groupWidth, layout.getMinWidth());
////                    height = layout.getPrefHeight();
////                } else {
////                    width = child.getWidth();
////                    height = child.getHeight();
////                }
//
//                float incrX = width + (x > 0 ? space : 0);
//                if (x + incrX > groupWidth && x > 0) {
//                    rowSizes.add(x);
//                    rowSizes.add(rowHeight);
//                    prefWidth = Math.max(prefWidth, x + pad);
//                    if (y > 0) y += wrapSpace;
//                    y += rowHeight;
//                    rowHeight = 0;
//                    x = 0;
//                    incrX = width;
//                }
//                x += incrX;
//                rowHeight = Math.max(rowHeight, height);
//            }
//            rowSizes.add(x);
//            rowSizes.add(rowHeight);
//            prefWidth = Math.max(prefWidth, x + pad);
//            if (y > 0) y += wrapSpace;
//            prefHeight = Math.max(prefHeight, y + rowHeight);
//            prefHeight += padTop + padBottom;
//            this.space = space;
////            if (round) {
////                prefWidth = Math.round(prefWidth);
////                prefHeight = Math.round(prefHeight);
////            }
        }

        public void layout () {
            wrappedLayout();
//            float prefWidth = getWidth();
//            float prefHeight = getHeight();
//            float wrapSpace = this.wrapSpace;
//            float padLeft = this.padLeft, padRight = this.padRight;
//            float maxWidth = prefWidth - padLeft - padRight;
//
//            SnapshotArray<Actor> children = getChildren();
//            int n = children.size;
//            float space = minSpace;
//            float width = cellWidth, height = cellHeight;
//            int canFit = 0;
//            if (n > 2) {
//                canFit = (int) (maxWidth / width);
//                space = MathUtils.clamp((maxWidth - canFit * width) / (canFit - 1), minSpace, maxSpace);
//            }
//
//            float rowY = prefHeight - padTop, groupWidth = getWidth(), xStart = padLeft, x = 0, rowHeight = 0, rowDir = -1;
//
//            rowY += getHeight() - prefHeight;
//
//            groupWidth -= padRight;
//
//            int i = 0, incr = 1;
//            for (int r = 0; i != n; i += incr) {
//                Actor child = children.get(i);
//
//                if (x + width - 20 > groupWidth || r == 0) {
//                    x = xStart;
//                    float rowWidth = canFit * width + (canFit - 1) * space;
//                    x += (maxWidth - rowWidth) / 2;
//                    rowHeight = height;
//                    if (r > 0) rowY += wrapSpace * rowDir;
//                    rowY += rowHeight * rowDir;
//                    r += 2;
//                }
//
//                float y = rowY;
//                y += rowHeight - height;
//
//                child.setBounds(x, y, width, height);
//                x += width + space;
//            }
        }

        private void wrappedLayout () {
            float padLeft = this.padLeft, padRight = this.padRight, pad = padLeft + padRight, space = this.space, wrapSpace = this.wrapSpace;
            float width = cellWidth, height = cellHeight;
            float maxWidth = getWidth() - pad;
            SnapshotArray<Actor> children = getChildren();
            int n = children.size;
            if (n < 1) {
                return;
            }

            if (n * width + (n - 1) * space <= maxWidth) { // strategy 1 - align.topLeft and fixed space
                System.out.println("case 1");
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
            } else if (n > 1) { // strategy 2 - align.topCenter and dynamic space between
                float rowY = getHeight() - padTop, groupWidth = getWidth(), xStart = padLeft, x = 0, rowHeight = 0, rowDir = -1;

                int canFit = (int) ((maxWidth - space) / (width + space));
                if (canFit > 1 && n > 1) {
                    System.out.println("case 2");
                    space = Math.max(space, (maxWidth - canFit * width) / (canFit - 1));

                    groupWidth -= padRight;

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
                        x += width + space;

                        if (layout != null) layout.validate();
                    }
                } else { // one item per row
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
                        x += width + space;

                        if (layout != null) layout.validate();
                    }
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
            float w = getWidth(), h = getHeight();
            float iw = (4.0f/5.0f) * w, ih = (4.0f/5.0f) * h;
            icon.setSize(iw, ih);
            icon.setPosition(getX() - iw / 2f + w / 2f, getY() - ih / 2f + h / 2f);
            icon.draw(batch, parentAlpha);
        }

        @Override
        public Item copyActor(Item copyFrom) {
            return null;
        }
    }
}
