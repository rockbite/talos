package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.scene.GameObject;
public abstract class AbstractImporter<T> {

    public abstract GameObject makeInstance (GameAsset<T> asset, GameObject parent);

}
