package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.esotericsoftware.spine.SkeletonData;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.meta.SpineMetadata;
import com.talosvfx.talos.runtime.scene.GameObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpineImporter extends AbstractImporter<SkeletonData> {


    private static final Logger logger = LoggerFactory.getLogger(SpineImporter.class);
    @Override
    public GameObject makeInstance (GameAsset<SkeletonData> asset, GameObject parent) {

        SpineMetadata metaData = (SpineMetadata)asset.getRootRawAsset().metaData;

        logger.info("Needs reimplementing for context");

//        SceneEditorWorkspace workspace = SceneEditorAddon.get().workspace;
//        Vector2 sceneCords = workspace.getMouseCordsOnScene();
//        GameObject gameObject = workspace.createSpineObject(asset, sceneCords, parent);
//
//        return gameObject;

        return null;
    }
}
