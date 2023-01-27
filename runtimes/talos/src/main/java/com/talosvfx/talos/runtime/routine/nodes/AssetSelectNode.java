package com.talosvfx.talos.runtime.routine.nodes;

import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.routine.RoutineNode;

public class AssetSelectNode extends RoutineNode {

    @Override
    public Object queryValue(String targetPortName) {
        GameAsset<?> asset = fetchAssetValue("asset");

        return asset;
    }
}
