package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.badlogic.gdx.Gdx;
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
    public FileHandle importAsset (FileHandle fileHandle) {
        String projectPath = SceneEditorAddon.get().workspace.getProjectPath();
        String assetPath = projectPath + File.separator + "assets";

        FileHandle importedAsset = importAssetFile(fileHandle); // skel file

        // copy atlas files too
        FileHandle atlasFile = AssetImporter.makeSimilar(fileHandle, "atlas");
        if(atlasFile.exists()) {
            FileHandle importedAtlasFile = Gdx.files.absolute(assetPath + File.separator + atlasFile.name());
            atlasFile.copyTo(importedAtlasFile);
            String data = importedAtlasFile.readString();
            String[] lines = data.split("\n");
            String pngName = lines[0];
            FileHandle pngFile = Gdx.files.absolute(atlasFile.parent().path() + "/" + pngName);
            FileHandle importedPngFile = Gdx.files.absolute(assetPath + File.separator + pngFile.name());
            pngFile.copyTo(importedPngFile);
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

        // read this from meta instead later
        spineRendererComponent.path = asset.parent().path() + "/" + asset.nameWithoutExtension() + ".atlas";
        skeletonComponent.path = asset.path();
        spineRendererComponent.reloadAtlas();
        skeletonComponent.setAtlas(spineRendererComponent.textureAtlas);
        skeletonComponent.reloadData();
    }

    @Override
    public FileHandle createMetadataFor (FileHandle handle) {
        FileHandle metadataHandle = AssetImporter.getMetadataHandleFor(handle);
        SpineMetadata metadata = new SpineMetadata();
        AssetImporter.saveMetadata(metadataHandle, metadata);

        return metadataHandle;
    }
}
