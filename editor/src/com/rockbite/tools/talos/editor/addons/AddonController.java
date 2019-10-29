package com.rockbite.tools.talos.editor.addons;

import com.badlogic.gdx.utils.Array;
import com.rockbite.tools.talos.editor.addons.bvb.BvBAddon;

public class AddonController {

    Array<IAddon> activeAddons = new Array();

    public AddonController() {
        registerAddon(new BvBAddon());
    }

    private void registerAddon(IAddon addon) {
        activeAddons.add(addon);
    }

    public void initAll() {
        for(IAddon addon: activeAddons) {
            addon.init();
        }
    }

}
