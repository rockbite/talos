package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.AtlasMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpineMetadata;

import java.io.File;

public class AtlasImporter extends AbstractImporter {
    @Override
    public void makeInstance (GameAsset asset, GameObject parent) {

    }

//    @Override
//    public FileHandle importAsset (FileHandle fileHandle, FileHandle destinationDir) {
//        FileHandle importedAsset = importAssetFile(fileHandle, destinationDir);
//
//        // todo: add support for multiple pages
//        String data = fileHandle.readString();
//        String[] lines = data.split("\n");
//        String pngName = lines[0];
//        FileHandle pngFile = Gdx.files.absolute(fileHandle.parent().path() + "/" + pngName);
//        FileHandle importedPngFile = Gdx.files.absolute(importedAsset.parent().path() + File.separator + pngFile.name());
//        pngFile.copyTo(importedPngFile);
//
//        return importedAsset;
//    }

//    @Override
//    public void makeInstance (FileHandle asset, GameObject parent) {
//        // do nothing
//    }

//    @Override
//    public FileHandle createMetadataFor (FileHandle handle) {
//        FileHandle metadataHandle = AssetImporter.getMetadataHandleFor(handle);
//        AtlasMetadata metadata = new AtlasMetadata();
//        AssetImporter.saveMetadata(metadataHandle, metadata);
//
//        return metadataHandle;
//    }
}
