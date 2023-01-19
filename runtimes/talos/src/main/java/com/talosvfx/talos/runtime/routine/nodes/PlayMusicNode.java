package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.audio.Music;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.routine.RoutineNode;

public class PlayMusicNode extends RoutineNode {

    private GameAsset<Music> asset;

    @Override
    public void receiveSignal(String portName) {
        asset = fetchAssetValue("asset");
        asset.getResource().play();
    }

    @Override
    public void reset() {
        super.reset();

        if(asset != null && asset.getResource() != null) {
            asset.getResource().stop();
        }
    }
}
