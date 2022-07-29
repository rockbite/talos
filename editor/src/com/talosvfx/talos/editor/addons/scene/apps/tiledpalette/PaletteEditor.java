package com.talosvfx.talos.editor.addons.scene.apps.tiledpalette;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.apps.AEditorApp;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.TilePaletteData;
import com.talosvfx.talos.editor.addons.scene.maps.GridPosition;
import com.talosvfx.talos.editor.addons.scene.maps.StaticTile;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.widgets.ui.common.SquareButton;

import java.util.UUID;

public class PaletteEditor extends AEditorApp<GameAsset<TilePaletteData>> {
    private String title;
    private DragAndDrop.Target target;
    private PaletteEditorWorkspace paletteEditorWorkspace;

    enum PaletteFilterMode {
        NONE,
        TILE,
        ENTITY,
        TILE_ENTITY
    }

    protected PaletteFilterMode currentFilterMode = PaletteFilterMode.TILE_ENTITY;

    public PaletteEditor(GameAsset<TilePaletteData> paletteData) {
        super(paletteData);
        identifier = paletteData.nameIdentifier;
        title = paletteData.nameIdentifier;
        initContent();
    }

    @Override
    public void initContent() {
        content = new Table();
        paletteEditorWorkspace = new PaletteEditorWorkspace(this.object);
        this.content.add(paletteEditorWorkspace).minSize(500).grow();

        Skin skin = TalosMain.Instance().getSkin();

        Table toolbar = new Table();
        toolbar.setFillParent(true);
        toolbar.top().left();

        Table element = new Table();
        element.setBackground(skin.newDrawable("button-main-menu"));
        toolbar.add(element).growX();

        element.defaults().pad(5);

        SquareButton tile = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-new"));
        SquareButton entity = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-new"));
        SquareButton tileEntity = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-new"));
        SquareButton delete = new SquareButton(skin, skin.getDrawable("timeline-btn-icon-new"));


        tile.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                tile.setChecked(!tile.isChecked());
                if (tile.isChecked()) {
                    currentFilterMode = PaletteFilterMode.TILE;
                }
            }
        });
        entity.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                entity.setChecked(!entity.isChecked());
                if (entity.isChecked()) {
                    currentFilterMode = PaletteFilterMode.ENTITY;
                }
            }
        });
        tileEntity.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                tileEntity.setChecked(!tileEntity.isChecked());
                if (tileEntity.isChecked()) {
                    currentFilterMode = PaletteFilterMode.TILE_ENTITY;
                }
            }
        });

        delete.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                for (GameAsset<?> selectedGameAsset : object.getResource().selectedGameAssets) {
                    if (currentFilterMode == PaletteFilterMode.TILE) {
                        removeSprite(selectedGameAsset);
                    } else if (currentFilterMode == PaletteFilterMode.ENTITY) {
                        removeEntity(selectedGameAsset);
                    } else { // currentFilterMode == PaletteFilterMode.TILE_ENTITY
                        if (object.getResource().staticTiles.containsKey(selectedGameAsset)) {
                            removeSprite(selectedGameAsset);
                        } else {
                            removeEntity(selectedGameAsset);
                        }
                    }
                }
            }
        });

        // mode buttons should react on palette changes
        paletteEditorWorkspace.addListener(new PaletteListener() {
            @Override
            public boolean selected(PaletteEvent e, GameAsset<?> gameAsset, PaletteFilterMode mode) {
                if (mode != PaletteFilterMode.NONE) {
                    currentFilterMode = mode;
                    tile.setChecked(false);
                    entity.setChecked(false);
                    tileEntity.setChecked(false);
                    switch (currentFilterMode) {
                        case TILE:
                            tile.setChecked(true);
                            break;
                        case ENTITY:
                            entity.setChecked(true);
                            break;
                        case TILE_ENTITY:
                            tileEntity.setChecked(true);
                            break;
                    }
                }
                return false;
            }
        });


        ButtonGroup<SquareButton> buttonButtonGroup = new ButtonGroup<>();
        buttonButtonGroup.add(tile, entity, tileEntity);
        buttonButtonGroup.setMaxCheckCount(1);
        buttonButtonGroup.setMinCheckCount(1);

        tileEntity.setChecked(true);

        element.add(tileEntity);
        element.add(tile);
        element.add(entity);
        element.add().expandX();
        element.add(delete);

        this.content.addActor(toolbar);

        // register the drag and drop target
        target = new PaletteDragAndDropTarget(paletteEditorWorkspace);
        SceneEditorAddon.get().projectExplorer.getDirectoryViewWidget().registerTarget(target);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public boolean notifyClose() {
        SceneEditorAddon.get().projectExplorer.getDirectoryViewWidget().unregisterTarget(target);
        return super.notifyClose();
    }

    public void addGameAsset (GameAsset<?> gameAsset, float x, float y) {
        //Check if we already have it
        GameAsset<TilePaletteData> tilePaletteGameAsset = object;

        TilePaletteData paletteData = tilePaletteGameAsset.getResource();
        ObjectMap<UUID, GameAsset<?>> references = paletteData.references;
        ObjectMap<UUID, float[]> positions = paletteData.positions;

        UUID gameAssetUUID = gameAsset.getRootRawAsset().metaData.uuid;
        if (references.containsKey(gameAssetUUID)) {
            System.out.println("Adding a duplicate, ignoring");
            return;
        }

        PaletteEditorWorkspace workspace = paletteEditorWorkspace;
        Vector2 worldSpace = workspace.getWorldFromLocal(x, y);

        references.put(gameAssetUUID, gameAsset);
        positions.put(gameAssetUUID, new float[]{worldSpace.x, worldSpace.y});

        tilePaletteGameAsset.dependentGameAssets.add(gameAsset);

        if (PaletteEditor.this.currentFilterMode == PaletteFilterMode.TILE) {
            if (gameAsset.type == GameAssetType.SPRITE) {
                addSprite(gameAsset);
            } else {
                System.out.println("Cannot add " + gameAsset.type + " in TILE mode");
            }
        } else { // PaletteEditor.this.currentFilterMode == PaletteFilterMode.ENTITY)
            addEntity(gameAsset);
        }

        AssetRepository.getInstance().saveGameAssetResourceJsonToFile(tilePaletteGameAsset);
    }

    public void removeGameAsset (GameAsset<?> gameAsset) {
        GameAsset<TilePaletteData> tilePaletteGameAsset = object;

        TilePaletteData paletteData = tilePaletteGameAsset.getResource();
        // first remove from selections
        paletteData.selectedGameAssets.removeValue(gameAsset, true);

        ObjectMap<UUID, GameAsset<?>> references = paletteData.references;
        ObjectMap<UUID, float[]> positions = paletteData.positions;

        UUID gameAssetUUID = gameAsset.getRootRawAsset().metaData.uuid;

        tilePaletteGameAsset.dependentGameAssets.removeValue(gameAsset, true);

        references.remove(gameAssetUUID);
        positions.remove(gameAssetUUID);

        AssetRepository.getInstance().saveGameAssetResourceJsonToFile(tilePaletteGameAsset);
    }

    public void addSprite(GameAsset<?> gameAsset) {
        object.getResource().addSprite(gameAsset);

    }

    public void removeSprite(GameAsset<?> gameAsset) {
        object.getResource().staticTiles.remove(gameAsset);
        removeGameAsset(gameAsset);
    }

    public void addEntity(GameAsset<?> gameAsset) {
        object.getResource().addEntity(gameAsset);
    }

    public void removeEntity(GameAsset<?> gameAsset) {
        object.getResource().gameObjects.remove(gameAsset);
        removeGameAsset(gameAsset);
    }

    public static boolean validForImport (GameAsset<?> gameAsset) {
        boolean valid = (gameAsset.type == GameAssetType.SPRITE) ||
                (gameAsset.type == GameAssetType.SKELETON) ||
                (gameAsset.type == GameAssetType.VFX) ||
                (gameAsset.type == GameAssetType.PREFAB);
        return valid;
    }

    private class PaletteDragAndDropTarget extends DragAndDrop.Target {

        public PaletteDragAndDropTarget(Actor actor) {
            super(actor);
        }

        @Override
        public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
            return payload.getObject() instanceof GameAsset<?> && validForImport((GameAsset<?>) payload.getObject());
        }

        @Override
        public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
            GameAsset<?> gameAsset = (GameAsset<?>) payload.getObject();
            addGameAsset(gameAsset, x, y);
        }
        // keep reference of what 'GameAsset' is selected. Our reference should be GameAsset type, but algorithms to select the entities/sprites can two combined
        // different approaches

        // also, when dropping sprite snap to grid
        // save after every edit event such as drop, remove or just move
        // draw mode such as brush and shit
        // main renderer
    }
}
