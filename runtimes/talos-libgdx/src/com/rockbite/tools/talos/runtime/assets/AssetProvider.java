package com.rockbite.tools.talos.runtime.assets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rockbite.tools.talos.runtime.render.TextureRegionDrawable;

public interface AssetProvider {

	TextureRegion findRegion (String regionName);

}
