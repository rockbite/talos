package com.rockbite.tools.talos.runtime.assets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public interface AssetProvider {

	TextureRegion findRegion (String regionName);

}
