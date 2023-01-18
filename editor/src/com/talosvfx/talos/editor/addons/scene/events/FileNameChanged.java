package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.editor.notifications.TalosEvent;


public class FileNameChanged implements TalosEvent {

   public GameAssetType assetType;
   public String oldName;
   public String newName;

    @Override
    public void reset() {
        assetType = null;
    }
}
