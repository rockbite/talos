package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.graphics.Texture;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;

public class AssetSelectNode extends RoutineNode {

    @Override
    public Object queryValue(String targetPortName) {
        GameAsset<Texture> asset = fetchAssetValue("asset");

        return asset;
    }
}
