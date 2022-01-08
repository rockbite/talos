package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.ParticleComponent;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.TlsMetadata;
import com.talosvfx.talos.editor.project.TalosProject;

public class TlsImporter extends AbstractImporter {

    public void exportTlsFile(FileHandle tlsHandle) {
        TalosMain.Instance().errorReporting.enabled = false;
        FileHandle exportLocation = AssetImporter.makeSimilar(tlsHandle, "p");
        TalosProject talosProject = new TalosProject();
        talosProject.loadProject(tlsHandle, tlsHandle.readString(), true);
        talosProject.exportProject(exportLocation);
        TalosMain.Instance().errorReporting.enabled = true;
    }

    @Override
    public FileHandle importAsset (FileHandle fileHandle) {
        FileHandle importedAsset = importAssetFile(fileHandle);

        FileHandle metadataHandle = AssetImporter.getMetadataHandleFor(importedAsset);
        TlsMetadata metadata = AssetImporter.readMetadata(metadataHandle, TlsMetadata.class);
        metadata.tlsChecksum = AssetImporter.checkSum(importedAsset);
        AssetImporter.saveMetadata(metadataHandle, metadata);

        exportTlsFile(importedAsset);

        return importedAsset;
    }

    @Override
    public void makeInstance (FileHandle asset, GameObject parent) {
        if(!AssetImporter.getMetadataHandleFor(asset).exists()) {
            createMetadataFor(asset);
        }
        TlsMetadata metadata = AssetImporter.readMetadataFor(asset, TlsMetadata.class);

        if(asset.extension().equals("tls")) {
            // if p doesnot exist, create it
            FileHandle pHandle = AssetImporter.makeSimilar(asset, "p");
            if(!pHandle.exists()) {
                // create
                exportTlsFile(pHandle);
            }

            asset = pHandle;
        }

        SceneEditorWorkspace workspace = SceneEditorAddon.get().workspace;
        Vector2 sceneCords = workspace.getMouseCordsOnScene();
        GameObject gameObject = workspace.createObjectByTypeName("particle", sceneCords, parent);

        ParticleComponent component = gameObject.getComponent(ParticleComponent.class);
        component.path = asset.path();
        component.reloadDescriptor();
    }

    @Override
    public FileHandle createMetadataFor (FileHandle handle) {
        FileHandle metadataHandle = AssetImporter.getMetadataHandleFor(handle);
        TlsMetadata metadata = new TlsMetadata();
        AssetImporter.saveMetadata(metadataHandle, metadata);

        return metadataHandle;
    }
}
