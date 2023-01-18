package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class BaseAssetRepository {
	public <U> GameAsset<U> getAssetForIdentifier (String identifier, GameAssetType type) {
		throw new GdxRuntimeException("Needs implementing");
	}

	public NinePatch obtainNinePatch (GameAsset<Texture> gameAsset) {
		return null;
	}
}
