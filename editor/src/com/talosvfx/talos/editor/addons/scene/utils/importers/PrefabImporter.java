package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.scene.GameObject;import com.talosvfx.talos.editor.addons.scene.logic.Prefab;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.PrefabMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class PrefabImporter extends AbstractImporter<Prefab> {

    private static final Logger logger = LoggerFactory.getLogger(PrefabImporter.class);
    @Override
    public GameObject makeInstance (GameAsset<Prefab> asset, GameObject parent) {



        PrefabMetadata metaData = (PrefabMetadata)asset.getRootRawAsset().metaData;
        UUID uuid = metaData.uuid;

        logger.info("Prefab Needs reimplementing for context");
//
//        SceneEditorWorkspace workspace = SceneEditorAddon.get().workspace;
//        Vector2 sceneCords = workspace.getMouseCordsOnScene();
//        GameObject gameObject = workspace.createFromPrefab(asset, sceneCords, parent);
//        gameObject.setPrefabLink(uuid.toString());

//        return gameObject;

        return null;
    }

}
