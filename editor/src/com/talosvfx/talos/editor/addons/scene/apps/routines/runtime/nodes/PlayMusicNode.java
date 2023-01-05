package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.audio.Music;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;

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
