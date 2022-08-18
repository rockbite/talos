package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.badlogic.gdx.files.FileHandle;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;

public class ScriptImporter extends AbstractImporter {
    @Override
    public GameObject makeInstance (GameAsset asset, GameObject parent) {
        return null;
    }

    public static void parsePropertiesFromHandle (FileHandle originalFile) {

    }
}
