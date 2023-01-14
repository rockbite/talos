package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.spine.SkeletonData;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpineMetadata;
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
