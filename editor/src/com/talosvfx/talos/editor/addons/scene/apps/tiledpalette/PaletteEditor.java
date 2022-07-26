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
import com.talosvfx.talos.editor.utils.GridDrawer;

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
            ObjectMap<UUID, GameAsset<?>> references = PaletteEditor.this.object.getResource().references;
            ObjectMap<UUID, float[]> positions = PaletteEditor.this.object.getResource().positions;
            references.put(gameAsset.getRootRawAsset().metaData.uuid, gameAsset);

            PaletteEditorWorkspace workspace = (PaletteEditorWorkspace) getActor();
            Vector2 pos = workspace.getWorldFromLocal(x, y);
            positions.put(gameAsset.getRootRawAsset().metaData.uuid, new float[]{pos.x, pos.y});
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
