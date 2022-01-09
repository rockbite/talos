package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.SkeletonComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpineRendererComponent;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpineMetadata;

import java.io.File;

public class SpineImporter extends AbstractImporter{

    @Override
    public FileHandle importAsset (FileHandle fileHandle, FileHandle destinationDir) {
        FileHandle importedAsset = importAssetFile(fileHandle, destinationDir); // skel file

        // import atlas files too
        FileHandle atlasFile = AssetImporter.makeSimilar(fileHandle, "atlas");
        if(atlasFile.exists()) {
            FileHandle importedAtlas = AssetImporter.attemptToImport(atlasFile);

            SpineMetadata spineMetadata = (SpineMetadata) AssetImporter.readMetadataFor(importedAsset);
            spineMetadata.atlasPath = AssetImporter.relative(importedAtlas.path());
            AssetImporter.saveMetadata(spineMetadata);
        }

        return importedAsset;
    }

    @Override
    public void makeInstance (FileHandle asset, GameObject parent) {
        SceneEditorWorkspace workspace = SceneEditorAddon.get().workspace;
        Vector2 sceneCords = workspace.getMouseCordsOnScene();
        GameObject gameObject = workspace.createObjectByTypeName("spine", sceneCords, parent);

        // now do some fancy component configuring from this guy
        SpineRendererComponent spineRendererComponent = gameObject.getComponent(SpineRendererComponent.class);
        SkeletonComponent skeletonComponent = gameObject.getComponent(SkeletonComponent.class);

        SpineMetadata spineMetadata = AssetImporter.readMetadataFor(asset, SpineMetadata.class);

        // read this from meta instead later
        spineRendererComponent.path = spineMetadata.atlasPath;
        skeletonComponent.path = AssetImporter.relative(asset.path());
        spineRendererComponent.reloadAtlas();
        skeletonComponent.setAtlas(spineRendererComponent.textureAtlas);
        skeletonComponent.reloadData(spineMetadata.scale);
    }

    @Override
    public FileHandle createMetadataFor (FileHandle handle) {
        FileHandle metadataHandle = AssetImporter.getMetadataHandleFor(handle);
        SpineMetadata metadata = new SpineMetadata();
        AssetImporter.saveMetadata(metadataHandle, metadata);

        return metadataHandle;
    }
}
