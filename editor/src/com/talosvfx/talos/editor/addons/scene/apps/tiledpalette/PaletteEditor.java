package com.talosvfx.talos.editor.addons.scene.apps.tiledpalette;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.apps.AEditorApp;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.TilePaletteData;

import java.util.UUID;

public class PaletteEditor extends AEditorApp<GameAsset<TilePaletteData>> {
    private String title;
    private DragAndDrop.Target target;

    public PaletteEditor(GameAsset<TilePaletteData> paletteData) {
        super(paletteData);
        identifier = paletteData.nameIdentifier;
        title = paletteData.nameIdentifier;
        initContent();
    }

    @Override
    public void initContent() {
        content = new Table();
        PaletteEditorWorkspace paletteEditorWorkspace = new PaletteEditorWorkspace(this.object);
        this.content.add(paletteEditorWorkspace).minSize(500).grow();

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

    private class PaletteDragAndDropTarget extends DragAndDrop.Target {

        public PaletteDragAndDropTarget(Actor actor) {
            super(actor);
        }

        @Override
        public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
            return payload.getObject() instanceof GameAsset<?>;
        }

        @Override
        public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
            GameAsset<?> gameAsset = (GameAsset<?>) payload.getObject();
            addGameAsset(gameAsset, x, y);
        }

        public void addGameAsset (GameAsset<?> gameAsset, float x, float y) {
            //Check if we already have it
            GameAsset<TilePaletteData> tilePaletteGameAsset = PaletteEditor.this.object;

            TilePaletteData paletteData = tilePaletteGameAsset.getResource();
            ObjectMap<UUID, GameAsset<?>> references = paletteData.references;
            ObjectMap<UUID, float[]> positions = paletteData.positions;

            UUID gameAssetUUID = gameAsset.getRootRawAsset().metaData.uuid;
            if (references.containsKey(gameAssetUUID)) {
                System.out.println("Adding a duplicate, ignoring");
                return;
            }


            PaletteEditorWorkspace workspace = (PaletteEditorWorkspace) getActor();
            Vector2 worldSpace = workspace.getWorldFromLocal(x, y);

            references.put(gameAssetUUID, gameAsset);
            positions.put(gameAssetUUID, new float[]{worldSpace.x, worldSpace.y});

            tilePaletteGameAsset.dependentGameAssets.add(gameAsset);
        }

        public void removeGameAsset (GameAsset<?> gameAsset) {
            GameAsset<TilePaletteData> tilePaletteGameAsset = PaletteEditor.this.object;

            TilePaletteData paletteData = tilePaletteGameAsset.getResource();
            ObjectMap<UUID, GameAsset<?>> references = paletteData.references;
            ObjectMap<UUID, float[]> positions = paletteData.positions;

            UUID gameAssetUUID = gameAsset.getRootRawAsset().metaData.uuid;

            tilePaletteGameAsset.dependentGameAssets.removeValue(gameAsset, true);

            references.remove(gameAssetUUID);
            positions.remove(gameAssetUUID);

        }

        public void addSprite() {

        }

        public void removeSprite() {

        }

        public void addEntity() {

        }

        public void removeEntity() {

        }

        // keep reference of what is selected
        // also when dropping sprite snap to grid
        // save after every edit event such as drop, remove or just move
        // draw mode such as brush and shit
        // main renderer
    }
}
