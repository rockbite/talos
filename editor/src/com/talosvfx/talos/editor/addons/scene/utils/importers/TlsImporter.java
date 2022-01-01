package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.badlogic.gdx.files.FileHandle;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.utils.AssetImporter;
import com.talosvfx.talos.editor.project.TalosProject;

public class TlsImporter {

    public static FileHandle run(FileHandle fileHandle) {
        FileHandle importedAsset = AssetImporter.importAssetFile(fileHandle);

        TalosMain.Instance().errorReporting.enabled = false;
        FileHandle exportLocation = AssetImporter.makeSimilar(importedAsset, "p");
        TalosProject talosProject = new TalosProject();
        talosProject.loadProject(importedAsset, importedAsset.readString(), true);
        talosProject.exportProject(exportLocation);
        TalosMain.Instance().errorReporting.enabled = true;

        return importedAsset;
    }
}
