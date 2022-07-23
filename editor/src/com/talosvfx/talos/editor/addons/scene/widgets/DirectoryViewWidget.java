package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.RawAsset;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectSelectionChanged;
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

public class DirectoryViewWidget extends Table {

    private Array<ItemView> items = new Array<>();
    private FileHandle fileHandle;
    private int colCount = 0;
    private float boxWidth = 0;

    private ItemView overItem;
    private Array<ItemView> selected = new Array<>();
    private ItemView selectCandidate;
    private int selectionStart = -1;

    private Vector2 tmp = new Vector2();

    private DragAndDrop dragAndDrop;

    private boolean preventDeselect = false;

    public DirectoryViewWidget() {
        build();
    }

    private void build () {

        dragAndDrop = new DragAndDrop();

        addListener(new ClickListener() {

            long timeClicked;
            Vector2 prevPos = new Vector2();

            @Override
            public boolean keyDown(InputEvent event, int keycode) {

                ItemView selectCandidate = getSelectCandidate();
                if(selectCandidate != null && SceneEditorWorkspace.isRenamePressed(keycode)) {
                    for(ItemView item: items) {
                        if(item.fileHandle.path().equals(selectCandidate.fileHandle.path())) {
                            // found it
                            item.setToRename();
                        }
                    }
                }

                ProjectExplorerWidget projectExplorer = SceneEditorAddon.get().projectExplorer;

                if(keycode == Input.Keys.X && SceneEditorWorkspace.ctrlPressed()) {
                    projectExplorer.invokeCut(convertToFileArray(selected));
                }

                if(keycode == Input.Keys.C && SceneEditorWorkspace.ctrlPressed()) {
                    projectExplorer.invokeCopy(convertToFileArray(selected));
                }

                if(keycode == Input.Keys.V && SceneEditorWorkspace.ctrlPressed()) {
                    projectExplorer.invokePaste(getCurrentFolder());
                }

                if(keycode == Input.Keys.FORWARD_DEL) {
                    Array<String> paths = new Array<>();
                    for(ItemView file: selected) {
                        paths.add(file.fileHandle.path());
                    }
                    projectExplorer.deletePath(paths);
                }

                if(keycode == Input.Keys.A && SceneEditorWorkspace.ctrlPressed()) {
                    selectAllFiles();
                    reportSelectionChanged();
                }

                return super.keyDown(event, keycode);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);

                if(preventDeselect) {
                    preventDeselect = false;
                    return;
                }

                ItemView itemViewAt = getFileAt(x, y);

                if(button == 1) {
                    if(itemViewAt != null) {
                        if (!selected.contains(itemViewAt, true)) {
                            selectFile(itemViewAt);
                            reportSelectionChanged();
                        }
                        SceneEditorAddon.get().projectExplorer.showContextMenu(convertToFileArray(selected), true);
                    } else {
                        Array<FileHandle> list = new Array<>();
                        list.add(fileHandle);
                        SceneEditorAddon.get().projectExplorer.showContextMenu(list, true);
                    }
                } else if(button == 0) {
                    float diff = TimeUtils.millis() - timeClicked;
                    ItemView fileToSelect = getFileAt(x, y);

                    if(fileToSelect != null) {
                        if(SceneEditorWorkspace.ctrlPressed()) {
                            if(selected.contains(fileToSelect, true)) {
                                removeFromSelection(fileToSelect);
                            } else {
                                addToSelection(fileToSelect);
                            }
                            reportSelectionChanged();
                            selectionStart = items.indexOf(fileToSelect, true);
                        } else {
                            if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                                if(selectionStart >= 0) {
                                    int aPoint = items.indexOf(fileToSelect, true);
                                    int bPoint = selectionStart;
                                    int min = Math.min(aPoint, bPoint);
                                    int max = Math.max(aPoint, bPoint);
                                    unselectFiles();
                                    for(int i = min; i <= max; i++) {
                                        addToSelection(items.get(i));
                                    }
                                    reportSelectionChanged();
                                }
                            } else {
                                selectionStart = items.indexOf(fileToSelect, true);
                                selectFile(fileToSelect);
                                reportSelectionChanged();
                            }
                        }
                    } else {
                        selectionStart = -1;
                        unselectFiles();
                        reportSelectionChanged();
                    }

                    if(diff < 500 && prevPos.dst(x, y) < 40) {
                        timeClicked = 0;
                        prevPos.set(0, 0);
                        ItemView fileAt = getFileAt(x, y);
                        if(fileAt != null) {
                            AssetImporter.fileOpen(fileAt.fileHandle);
                        } else {
                            // go up
                            if(fileHandle != null) {
                                FileHandle parent = fileHandle.parent();
                                FileHandle projectFolder = SceneEditorAddon.get().workspace.getProjectFolder();
                                if(parent.path().startsWith(projectFolder.path())) {
                                    SceneEditorAddon.get().projectExplorer.select(parent.path());
                                }

                            }
                        }
                    }

                    timeClicked = TimeUtils.millis();
                    prevPos.set(x, y);
                }

                if(getStage() != null) {
                    getStage().setKeyboardFocus(DirectoryViewWidget.this);
                }
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {

                ItemView item = getFileAt(x, y);

                overItem = item;

                return super.mouseMoved(event, x, y);
            }
        });

        setTouchable(Touchable.enabled);
    }

    private Array<FileHandle> convertToFileArray (Array<ItemView> selected) {
        Array<FileHandle> handles = new Array<>();
        for (ItemView itemView : selected) {
            handles.add(itemView.fileHandle);
        }
        return handles;
    }

    private void removeFromSelection(ItemView itemView) {
        selected.removeValue(itemView, true);
    }

    private void addToSelection(ItemView itemView) {
        selected.add(itemView);
        selectCandidate = itemView;
        getStage().setKeyboardFocus(this);
    }

    private void selectFile(ItemView itemView) {
        selected.clear();
        selected.add(itemView);
        selectCandidate = itemView;
        getStage().setKeyboardFocus(this);
    }

    private ItemView getSelectCandidate() {
        if(selectCandidate != null) return selectCandidate;

        if(selected.size > 0) {
            return selected.first();
        }

        return null;
    }

    private void selectAllFiles() {
        selected.clear();
        for(ItemView view: items) {
            selected.add(view);
        }
    }

    private void unselectFiles() {
        selected.clear();
        selectCandidate = null;
    }


    private ItemView getFileAt(float x, float y) {
        for(ItemView view: items) {
            tmp.set(x, y);
            localToStageCoordinates(tmp);
            view.stageToLocalCoordinates(tmp);

            if(view.hit(tmp.x, tmp.y, false) != null) {
                return view;
            }
        }
        return null;
    }

    public void setDirectory(String path) {
        fileHandle = Gdx.files.absolute(path);
        if (fileHandle == null || !fileHandle.exists()) {
            System.out.println("Error setting dir " + path);
            return;
        };

        rebuild();
    }

    private void rebuild () {
        if(fileHandle == null || !fileHandle.exists()) return;

        dragAndDrop.clear();
        clearChildren();
        items.clear();

        dragAndDrop.addTarget(new DragAndDrop.Target(SceneEditorAddon.get().workspace) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {

                Actor actor = getActor();

                return true;
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                Actor targetActor = getActor();

                Object object = payload.getObject();
                if(object instanceof Array) {

                    //its a selection of File handles

                    Array<ItemView> array = (Array<ItemView>) object;

                    for (ItemView itemView : array) {
                        if (itemView.gameAsset != null) {
                            AssetImporter.createAssetInstance(itemView.gameAsset, SceneEditorAddon.get().workspace.getRootGO());
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

                unselectFiles();
            }
        });
        dragAndDrop.addTarget(new DragAndDrop.Target(SceneEditorAddon.get().hierarchy) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                Actor actor = getActor();

                return true;
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                Actor targetActor = getActor();
            }
        });

        for (ObjectMap.Entry<String, FilteredTree.Node> node : SceneEditorAddon.get().projectExplorer.getNodes()) {
            dragAndDrop.addTarget(new DragAndDrop.Target(node.value.getActor()) {
                @Override
                public boolean drag (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                    return true;
                }

                @Override
                public void drop (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {

                    //Payload is either an Array<ItemView>, Or its a FileHandle, Or its a GameAsset

                    fileHandle = ((ProjectExplorerWidget.RowWidget) getActor()).getFileHandle();

                    Object object = payload.getObject();
                    if (object instanceof Array) {
                        Array<ItemView> array = (Array<ItemView>) object;
                        for (ItemView sourceItem : array) {
                            if (!sourceItem.fileHandle.path().equals(fileHandle.path())) {
                                AssetImporter.moveFile(sourceItem.fileHandle, fileHandle);
                            }
                        }
                    } else if (object instanceof GameAsset) {
                        GameAsset<?> sourceItem = (GameAsset) payload.getObject();
                        FileHandle handle = sourceItem.getRootRawAsset().handle;
                        if (!handle.path().equals(fileHandle.path())) {
                            AssetImporter.moveFile(handle, fileHandle);
                        }
                    } else if (object instanceof FileHandle) {
                        FileHandle handle = (FileHandle) payload.getObject();
                        if (!handle.path().equals(fileHandle.path())) {
                            AssetImporter.moveFile(handle, fileHandle);
                        }
                    }
                    rebuild();
                }
            });
        }

        dragAndDrop.addTarget(new DragAndDrop.Target(this) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {

                ItemView fileAt = getFileAt(x, y);

                if(fileAt != null) {
                    overItem = fileAt;
                    return true;
                }

                return false;
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                ItemView targetItem = getFileAt(x, y);

                if(targetItem.fileHandle.isDirectory() && !targetItem.fileHandle.path().equals(fileHandle.path())) {

                    Object object = payload.getObject();
                    if (object instanceof Array) {
                        Array<ItemView> array = (Array<ItemView>) object;
                        for (ItemView sourceItem : array) {
                            if (!sourceItem.fileHandle.path().equals(targetItem.fileHandle.path())) {
                                AssetImporter.moveFile(sourceItem.fileHandle, targetItem.fileHandle);
                            }
                        }
                    } else if (object instanceof GameAsset) {
                        GameAsset sourceItem = (GameAsset) payload.getObject();
                        FileHandle handle = sourceItem.getRootRawAsset().handle;
                        if (!handle.path().equals(targetItem.fileHandle.path())) {
                            AssetImporter.moveFile(handle, targetItem.fileHandle);
                        }
                    } else if (object instanceof FileHandle) {
                        FileHandle handle = (FileHandle) payload.getObject();
                        if (!handle.path().equals(targetItem.fileHandle.path())) {
                            AssetImporter.moveFile(handle, targetItem.fileHandle);
                        }
                    }

                    unselectFiles();
                }
            }
        });



        if (fileHandle.isDirectory()) {
            int count = 0;
            for (FileHandle child : fileHandle.list()) {
                if(!ProjectExplorerWidget.fileFilter.accept(child.file())) {
                    continue;
                }

                ItemView itemView = new ItemView();
                itemView.setFile(child);

                items.add(itemView);
                itemView.pack();
                boxWidth = itemView.getWidth();

                dragAndDrop.addSource(new DragAndDrop.Source(itemView) {
                    @Override
                    public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                        DragAndDrop.Payload payload = new DragAndDrop.Payload();

                        preventDeselect = true;

                        if(!selected.contains(itemView, true)) {
                            selected.clear();
                            selected.add(itemView);
                        }

                        if(selected.size == 1) {
                            ItemView newView = new ItemView();
                            Actor dragging = ((ActorCloneable) newView).copyActor(itemView);
                            payload.setDragActor(dragging);
                            if (newView.gameAsset != null) {
                                payload.setObject(newView.gameAsset);
                            } else {
                                payload.setObject(child);
                            }
                        } else {
                            Label dragging = new Label("Multiple selection", TalosMain.Instance().getSkin());
                            payload.setDragActor(dragging);
                            payload.setObject(selected);
                        }

                        return payload;
                    }
                });

                colCount = (int) (getWidth() / (boxWidth + 10)) - 1;

                add(itemView).top().left().width(120).padLeft(10).padTop(10);
                count++;
                if (count >= colCount) {
                    count = 0;
                    add().expandX().growX();
                    row();
                }
            }
            add().colspan(colCount-count).expandX().growX();
            row();
            add().colspan(colCount).expand().grow();
        }

        if(getStage() != null) {
            getStage().setKeyboardFocus(DirectoryViewWidget.this);
        }
    }

    @Override
    protected void sizeChanged () {
        super.sizeChanged();

        if(boxWidth > 0) {

            int newColCount = (int) (getWidth() / boxWidth);

            if(colCount != newColCount) {
                rebuild();
            }
        }
    }

    public void reload () {
        rebuild();
    }

    public FileHandle getCurrentFolder () {
        return fileHandle;
    }

    public void startRenameFor(FileHandle handle) {
        for(ItemView itemView: items) {
            if(itemView.fileHandle.path().equals(handle.path())) {
                itemView.setToRename();
            }
        }
    }

    public class ItemView extends Table implements ActorCloneable<ItemView> {

        private Table iconContainer;
        private Image icon;
        private EditableLabel label;

        private FileHandle fileHandle;
        private GameAsset<?> gameAsset;

        private GameObject basicGameObject;

        public ItemView() {
            build();
        }

        private void build() {
            Skin skin = TalosMain.Instance().getSkin();
            icon = new Image();
            label = new EditableLabel("text", skin);
            label.setAlignment(Align.center);
            label.getLabel().setWrap(true);
            label.getLabelCell().width(100);

            label.setListener(new EditableLabel.EditableLabelChangeListener() {
                @Override
                public void changed(String newText) {
                    if(newText.isEmpty()) {
                        newText = fileHandle.nameWithoutExtension();
                    }
                    FileHandle newHandle = AssetImporter.renameFile(fileHandle, newText);
                    if(newHandle.isDirectory()) {
                        SceneEditorAddon.get().projectExplorer.notifyRename(fileHandle, newHandle);
                    }
                    fileHandle = newHandle;
                    setFile(fileHandle);
                }
            });

            iconContainer = new Table();
            iconContainer.add(icon).grow();

            add(iconContainer).size(90).pad(10);
            row();
            add(label).width(70).center().pad(5);

            setTouchable(Touchable.enabled);
        }

        public void setToRename() {
            label.setEditMode();
        }

        private void setFile (FileHandle fileHandle) {

            String name = fileHandle.name();
            label.setText(name);
            label.getLabel().setWrap(true);
            label.getLabel().setEllipsis(true);

            if(fileHandle.isDirectory()) {
                icon.setDrawable(TalosMain.Instance().getSkin().getDrawable("ic-folder-big"));
            } else {
//                icon.setDrawable(TalosMain.Instance().getSkin().getDrawable("ic-file-big"));
//                String extension = fileHandle.extension();
//                if(extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg")) {
//                    Texture texture = new Texture(fileHandle);
//                    TextureRegionDrawable drawable = new TextureRegionDrawable(texture);
//                    icon.setDrawable(drawable);
//                    icon.setScaling(Scaling.fit);
//                }
            }

            this.fileHandle = fileHandle;

            GameAsset<?> assetForPath = AssetRepository.getInstance().getAssetForPath(fileHandle, true);
            if (assetForPath != null) {
                gameAsset = assetForPath;
            }

            if (assetForPath != null) {
                //Lets add something to the icon so it shows
                Image image = new Image(TalosMain.Instance().getSkin().getDrawable("ic-fileset-file"));
                iconContainer.addActor(image);
                if (assetForPath.isBroken()) {
                    image.setColor(Color.RED);
                } else {
                    image.setColor(Color.GREEN);

                    //Game asset is legit, lets try to make one
                    GameObject parent = new GameObject();
                    parent.addComponent(new TransformComponent());
                    basicGameObject = parent;

                    AssetImporter.fromDirectoryView = true; //tom is very naughty dont be like tom
                    boolean success = AssetImporter.createAssetInstance(assetForPath, parent);
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


                        GameObjectActor gameObjectActor = new GameObjectActor(SceneEditorWorkspace.getInstance().getUISceneRenderer(), basicGameObject, copy, true);
                        gameObjectActor.setFillParent(true);
                        iconContainer.addActor(gameObjectActor);
                    }
                }
            }
        }

        @Override
        public void act(float delta) {
            Skin skin = TalosMain.Instance().getSkin();
            super.act(delta);

            for(ItemView view: items) {
                if(view != overItem) {
                    view.setBackground((Drawable) (null));
                }

                if(selected.contains(view, true)) {
                    view.setBackground(ColorLibrary.obtainBackground(skin, "white", ColorLibrary.BackgroundColor.DARK_GRAY));
                }
            }

            if(overItem != null) {
                overItem.setBackground(ColorLibrary.obtainBackground(skin, "white", ColorLibrary.BackgroundColor.LIGHT_GRAY));
            }
        }

        @Override
        public ItemView copyActor(ItemView copyFrom) {
            setFile(copyFrom.fileHandle);
            return this;
        }
    }

    private void reportSelectionChanged() {
        if(!selected.isEmpty()) {
            IPropertyHolder holder = null;
            if (selected.size == 1) {
                ItemView item = selected.first();
                if (item.gameAsset != null) {
                    holder = item.gameAsset.getRootRawAsset().metaData;
                }

            } else if (selected.size > 1) {
                Array<AMetadata> list = new Array<AMetadata>();
                for (ItemView item : selected) {
                    if (item.gameAsset != null) {
                        RawAsset rootRawAsset = item.gameAsset.getRootRawAsset();
                        list.add(rootRawAsset.metaData);
                    }
                }
                holder = new MultiPropertyHolder(list);
            }

            if(holder != null) {
                Notifications.fireEvent(Notifications.obtainEvent(PropertyHolderSelected.class).setTarget(holder));
            }
        }
    }
}


