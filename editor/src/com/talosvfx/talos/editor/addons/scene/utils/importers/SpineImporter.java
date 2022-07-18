package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.spine.SkeletonData;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpineMetadata;

public class SpineImporter extends AbstractImporter<SkeletonData> {

//    @Override
//    public FileHandle importAsset (FileHandle fileHandle, FileHandle destinationDir) {
//        FileHandle importedAsset = importAssetFile(fileHandle, destinationDir); // skel file
//
//        // import atlas files too
//        FileHandle atlasFile = AssetImporter.makeSimilar(fileHandle, "atlas");
//        if(atlasFile.exists()) {
//            FileHandle importedAtlas = AssetImporter.attemptToImport(atlasFile);
//
//            SpineMetadata spineMetadata = (SpineMetadata) AssetImporter.readMetadataFor(importedAsset);
//            spineMetadata.atlasPath = AssetImporter.relative(importedAtlas.path());
//            AssetImporter.saveMetadata(spineMetadata);
//        }
//
//        return importedAsset;
//    }

//    @Override
//    public void makeInstance (FileHandle asset, GameObject parent) {
//        SceneEditorWorkspace workspace = SceneEditorAddon.get().workspace;
//        Vector2 sceneCords = workspace.getMouseCordsOnScene();
//        GameObject gameObject = workspace.createObjectByTypeName("spine", sceneCords, parent);
//
//        // now do some fancy component configuring from this guy
//        SpineRendererComponent spineRendererComponent = gameObject.getComponent(SpineRendererComponent.class);
//        SkeletonComponent skeletonComponent = gameObject.getComponent(SkeletonComponent.class);
//
//        SpineMetadata spineMetadata = AssetImporter.readMetadataFor(asset, SpineMetadata.class);
//
//        // read this from meta instead later
//        spineRendererComponent.path = spineMetadata.atlasPath;
//        skeletonComponent.path = AssetImporter.relative(asset.path());
//        spineRendererComponent.reloadAtlas();
//        skeletonComponent.setAtlas(spineRendererComponent.textureAtlas);
//        skeletonComponent.reloadData(spineMetadata.scale);
//    }


    @Override
    public void makeInstance (GameAsset<SkeletonData> asset, GameObject parent) {

        SpineMetadata metaData = (SpineMetadata)asset.getRootRawAsset().metaData;

        SceneEditorWorkspace workspace = SceneEditorAddon.get().workspace;
        Vector2 sceneCords = workspace.getMouseCordsOnScene();
        GameObject gameObject = workspace.createSpineObject(asset, sceneCords, parent);

    }
}
