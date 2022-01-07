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
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.widgets.ui.ActorCloneable;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class DirectoryViewWidget extends Table {

    private Array<ItemView> items = new Array<>();
    private FileHandle fileHandle;
    private int colCount = 0;
    private float boxWidth = 0;

    private ItemView overItem;
    private Array<FileHandle> selected = new Array<>();

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
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);

                float diff = TimeUtils.millis() - timeClicked;

                ItemView fileToSelect = getFileAt(x, y);
                if(fileToSelect != null) {
                    if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                        if(selected.contains(fileToSelect.fileHandle, true)) {
                            removeFromSelection(fileToSelect.fileHandle);
                        } else {
                            addToSelection(fileToSelect.fileHandle);
                        }
                    } else {
                        selectFile(fileToSelect.fileHandle);
                    }
                } else {
                    unselectFiles();
                }

                if(diff < 500 && prevPos.dst(x, y) < 40) {
                    timeClicked = 0;
                    prevPos.set(0, 0);
                    ItemView fileAt = getFileAt(x, y);
                    if(fileAt != null) {
                        if(fileAt.fileHandle.isDirectory()) {
                            SceneEditorAddon.get().projectExplorer.select(fileAt.fileHandle.path());
                        } else {
                            // maybe custom open it or something
                        }
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
    }

    private void selectFile(FileHandle fileHandle) {
        selected.clear();
        selected.add(fileHandle);
    }


    private void unselectFiles() {
        selected.clear();
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

        if (fileHandle.isDirectory()) {
            int count = 0;
            for (FileHandle child : fileHandle.list()) {
                if(!ProjectExplorerWidget.fileFilter.accept(child.file())) {
                    continue;
                }

                ItemView itemView = new ItemView();
                itemView.setFile(child);

                Table box = new Table();
                box.add(itemView).pad(10).expand().left().top().padTop(4);
                items.add(itemView);
                box.pack();
                boxWidth = box.getWidth();

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

                colCount = (int) (getWidth() / boxWidth);

                add(box).top().left().width(100).padLeft(10).padTop(10);
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

    public class ItemView extends Table implements ActorCloneable<ItemView> {

        private Image icon;
        private Label label;

        private FileHandle fileHandle;

        public ItemView() {
            build();
        }

        private void build() {
            Skin skin = TalosMain.Instance().getSkin();
            icon = new Image();
            label = new Label("text", skin);
            label.setEllipsis(true);
            label.setAlignment(Align.center);

            Table iconContainer = new Table();
            iconContainer.add(icon).grow();

            add(iconContainer).size(70).pad(10);
            row();
            add(label).expandX().width(85).center().pad(5);

            setTouchable(Touchable.enabled);
        }

        public void setFile(FileHandle fileHandle) {
            label.setText(fileHandle.name());
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
}


