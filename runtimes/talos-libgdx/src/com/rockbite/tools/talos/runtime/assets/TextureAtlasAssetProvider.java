package com.rockbite.tools.talos.runtime.assets;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureAtlasAssetProvider implements AssetProvider {

	private TextureAtlas atlas;

	public TextureAtlasAssetProvider (TextureAtlas atlas) {
		this.atlas = atlas;
	}

	@Override
	public TextureRegion findRegion (String regionName) {
		return atlas.findRegion(regionName);
	}
}
