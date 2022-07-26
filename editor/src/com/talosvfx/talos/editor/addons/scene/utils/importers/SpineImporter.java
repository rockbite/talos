package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.spine.SkeletonData;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpineMetadata;

public class SpineImporter extends AbstractImporter<SkeletonData> {


    @Override
    public void makeInstance (GameAsset<SkeletonData> asset, GameObject parent) {

        SpineMetadata metaData = (SpineMetadata)asset.getRootRawAsset().metaData;

        SceneEditorWorkspace workspace = SceneEditorAddon.get().workspace;
        Vector2 sceneCords = workspace.getMouseCordsOnScene();
        GameObject gameObject = workspace.createSpineObject(asset, sceneCords, parent);

    }
}
