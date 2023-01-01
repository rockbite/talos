package com.talosvfx.talos.editor.addons.scene.events.vfx;

import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.notifications.TalosEvent;
import com.talosvfx.talos.editor.serialization.VFXProjectData;

public class VFXPreviewActivated implements TalosEvent {
    public GameAsset<VFXProjectData> asset;

    @Override
    public void reset() {
        asset = null;
    }

    public TalosEvent set(GameAsset<VFXProjectData> gameAsset) {
        asset = gameAsset;
        return this;
    }
}
