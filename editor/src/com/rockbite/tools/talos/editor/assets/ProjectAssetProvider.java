package com.rockbite.tools.talos.editor.assets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rockbite.tools.talos.runtime.assets.AssetProvider;

public class ProjectAssetProvider implements AssetProvider {

	private TextureAtlas atlas;

	public ProjectAssetProvider () {
		atlas = new TextureAtlas();
	}

	public void addTextureAsTextureRegion (String name, Texture texture) {
		atlas.addRegion(name, new TextureRegion(texture));
	}

	@Override
	public TextureRegion findRegion (String regionName) {
		return atlas.findRegion(regionName);
	}
}
