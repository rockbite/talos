package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.utils.GdxRuntimeException;

public class BaseAssetRepository {
	public <U> GameAsset<U> getAssetForIdentifier (String identifier, GameAssetType type) {
		throw new GdxRuntimeException("Needs implementing");
	}
}
