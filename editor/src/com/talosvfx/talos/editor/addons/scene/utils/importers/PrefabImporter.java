package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.Prefab;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.PrefabMetadata;

import java.util.UUID;

public class PrefabImporter extends AbstractImporter<Prefab> {
    @Override
    public GameObject makeInstance (GameAsset<Prefab> asset, GameObject parent) {

        PrefabMetadata metaData = (PrefabMetadata)asset.getRootRawAsset().metaData;
        UUID uuid = metaData.uuid;

        SceneEditorWorkspace workspace = SceneEditorAddon.get().workspace;
        Vector2 sceneCords = workspace.getMouseCordsOnScene();
        GameObject gameObject = workspace.createFromPrefab(asset, sceneCords, parent);
        gameObject.setPrefabLink(uuid.toString());

        return gameObject;
    }

}
