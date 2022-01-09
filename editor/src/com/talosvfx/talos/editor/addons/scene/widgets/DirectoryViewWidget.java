package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.TimeUtils;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectSelectionChanged;
import com.talosvfx.talos.editor.addons.scene.events.PropertyHolderSelected;
import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.logic.MultiPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.widgets.ui.ActorCloneable;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

import java.io.File;

public class DirectoryViewWidget extends Table {

    private Array<ItemView> items = new Array<>();
    private FileHandle fileHandle;
    private int colCount = 0;
    private float boxWidth = 0;

    private ItemView overItem;
    private Array<FileHandle> selected = new Array<>();
    private FileHandle selectCandidate;
    private int selectionStart = -1;

    private Vector2 tmp = new Vector2();

    private DragAndDrop dragAndDrop;

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

                FileHandle selectCandidate = getSelectCandidate();
                if(selectCandidate != null && SceneEditorWorkspace.isRenamePressed(keycode)) {
                    for(ItemView item: items) {
                        if(item.fileHandle.path().equals(selectCandidate.path())) {
                            // found it
                            item.setToRename();
                        }
                    }
                }

                ProjectExplorerWidget projectExplorer = SceneEditorAddon.get().projectExplorer;

                if(keycode == Input.Keys.X && SceneEditorWorkspace.ctrlPressed()) {
                    projectExplorer.invokeCut(selected);
                }

                if(keycode == Input.Keys.C && SceneEditorWorkspace.ctrlPressed()) {
                    projectExplorer.invokeCopy(selected);
                }

                if(keycode == Input.Keys.V && SceneEditorWorkspace.ctrlPressed()) {
                    projectExplorer.invokePaste(getCurrentFolder());
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

                ItemView fileToContext = getFileAt(x, y);

                if(button == 1) {
                    if(fileToContext != null) {
                        if (!selected.contains(fileToContext.fileHandle, true)) {
                            selectFile(fileToContext.fileHandle);
                            reportSelectionChanged();
                        }
                        SceneEditorAddon.get().projectExplorer.showContextMenu(selected);
                    } else {
                        Array<FileHandle> list = new Array<>();
                        list.add(fileHandle);
                        SceneEditorAddon.get().projectExplorer.showContextMenu(list);
                    }
                } else if(button == 0) {
                    float diff = TimeUtils.millis() - timeClicked;
                    ItemView fileToSelect = getFileAt(x, y);

                    if(fileToSelect != null) {
                        if(SceneEditorWorkspace.ctrlPressed()) {
                            if(selected.contains(fileToSelect.fileHandle, true)) {
                                removeFromSelection(fileToSelect.fileHandle);
                            } else {
                                addToSelection(fileToSelect.fileHandle);
                            }
                            reportSelectionChanged();
                            selectionStart = items.indexOf(fileToSelect, true);
                        } else {
                            if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                                if(selectionStart > 0) {
                                    int aPoint = items.indexOf(fileToSelect, true);
                                    int bPoint = selectionStart;
                                    int min = Math.min(aPoint, bPoint);
                                    int max = Math.max(aPoint, bPoint);
                                    unselectFiles();
                                    for(int i = min; i <= max; i++) {
                                        addToSelection(items.get(i).fileHandle);
                                    }
                                    reportSelectionChanged();
                                }
                            } else {
                                selectionStart = items.indexOf(fileToSelect, true);
                                selectFile(fileToSelect.fileHandle);
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

    private void removeFromSelection(FileHandle fileHandle) {
        selected.removeValue(fileHandle, true);
    }

    private void addToSelection(FileHandle fileHandle) {
        selected.add(fileHandle);
        selectCandidate = fileHandle;
        getStage().setKeyboardFocus(this);
    }

    private void selectFile(FileHandle fileHandle) {
        selected.clear();
        selected.add(fileHandle);
        selectCandidate = fileHandle;
        getStage().setKeyboardFocus(this);
    }

    private FileHandle getSelectCandidate() {
        if(selectCandidate != null) return selectCandidate;

        if(selected.size > 0) {
            return selected.first();
        }

        return null;
    }

    private void selectAllFiles() {
        selected.clear();
        for(ItemView view: items) {
            selected.add(view.fileHandle);
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
        if(fileHandle == null || !fileHandle.exists()) return;

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
                AssetImporter.createAssetInstance((FileHandle) payload.getObject(), SceneEditorAddon.get().workspace.getRootGO());
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
                FileHandle sourceItem = (FileHandle) payload.getObject();

                if(!sourceItem.path().equals(targetItem.fileHandle.path())) {
                    moveItemTo(sourceItem, targetItem.fileHandle);
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

                        ItemView newVIew = new ItemView();
                        Actor dragging = ((ActorCloneable) newVIew).copyActor(itemView);

                        payload.setDragActor(dragging);
                        payload.setObject(child);

                        return payload;
                    }
                });

                colCount = (int) (getWidth() / (boxWidth + 10)) - 1;

                add(itemView).top().left().width(100).padLeft(10).padTop(10);
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
    }

    private void moveItemTo(FileHandle source, FileHandle target) {
        if(target.isDirectory()) {
            String folderName = source.name();
            String newPath = target.path() + File.separator + folderName;

            FileHandle destination = Gdx.files.absolute(newPath);
            source.moveTo(destination);
        }

        SceneEditorAddon.get().workspace.reloadProjectExplorer();
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

    public class ItemView extends Table implements ActorCloneable<ItemView> {

        private Image icon;
        private EditableLabel label;

        private FileHandle fileHandle;

        public ItemView() {
            build();
        }

        private void build() {
            Skin skin = TalosMain.Instance().getSkin();
            icon = new Image();
            label = new EditableLabel("text", skin);
            label.setAlignment(Align.center);

            label.setListener(new EditableLabel.EditableLabelChangeListener() {
                @Override
                public void changed(String newText) {
                    if(!fileHandle.isDirectory()) {
                        String extension = fileHandle.extension();
                        if (!newText.contains(".")) newText += "." + extension;
                    }

                    FileHandle parent = fileHandle.parent();
                    FileHandle newHandle = Gdx.files.absolute(parent.path() + File.separator + newText);

                    if(newHandle.path().equals(fileHandle.path())) return;

                    fileHandle.moveTo(newHandle);
                    fileHandle = newHandle;
                    setFile(fileHandle);
                }
            });

            Table iconContainer = new Table();
            iconContainer.add(icon).grow();

            add(iconContainer).size(70).pad(10);
            row();
            add(label).expandX().width(50).center().pad(5);

            setTouchable(Touchable.enabled);
        }

        public void setToRename() {
            label.setEditMode();
        }

        public void setFile(FileHandle fileHandle) {

            String name = fileHandle.name();
            int len = 12;
            if(name.length() > len) name = name.substring(0, len-3) + "...";
            label.setText(name);

            if(fileHandle.isDirectory()) {
                icon.setDrawable(TalosMain.Instance().getSkin().getDrawable("ic-folder-big"));
            } else {
                icon.setDrawable(TalosMain.Instance().getSkin().getDrawable("ic-file-big"));
                if(fileHandle.extension().equals("png")) {
                    Texture texture = new Texture(fileHandle);
                    TextureRegionDrawable drawable = new TextureRegionDrawable(texture);
                    icon.setDrawable(drawable);
                    icon.setScaling(Scaling.fit);
                }
            }

            this.fileHandle = fileHandle;        }

        @Override
        public void act(float delta) {
            Skin skin = TalosMain.Instance().getSkin();
            super.act(delta);

            for(ItemView view: items) {
                if(view != overItem) {
                    view.setBackground((Drawable) (null));
                }

                if(selected.contains(view.fileHandle, true)) {
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
                FileHandle item = selected.first();
                holder = AssetImporter.readMetadataFor(item);

            } else if (selected.size > 1) {
                Array<AMetadata> list = new Array<AMetadata>();
                for (FileHandle item : selected) {
                    AMetadata aMetadata = AssetImporter.readMetadataFor(item);
                    list.add(aMetadata);
                }
                holder = new MultiPropertyHolder(list);
            }

            if(holder != null) {
                Notifications.fireEvent(Notifications.obtainEvent(PropertyHolderSelected.class).setTarget(holder));
            }
        }
    }
}


