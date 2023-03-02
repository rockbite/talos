/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.talosvfx.talos.runtime.vfx.modules;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.BaseAssetRepository;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.GameResourceOwner;
import com.talosvfx.talos.runtime.vfx.ParticleEmitterDescriptor;
import com.talosvfx.talos.runtime.vfx.values.ModuleValue;


public class SpriteMaterialModule extends MaterialModule implements GameResourceOwner<Texture>, GameAsset.GameAssetUpdateListener {


	private transient TextureRegion region;

	public GameAsset<Texture> asset;

	/**
	 * To be removed, here for backwards compatibility
	 */
	@Deprecated
	private String assetIdentifier = "white";
	private ModuleValue<SpriteMaterialModule> moduleOutput;

	@Override
	protected void defineSlots () {
		moduleOutput = new ModuleValue<>();
		moduleOutput.setModule(this);

		createOutputSlot(MATERIAL_MODULE, moduleOutput);
	}

	@Override
	public void processCustomValues () {
	}

	@Override
	public void write (Json json) {
		super.write(json);

		GameResourceOwner.writeGameAsset(json, this);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);

		//deprecated
		assetIdentifier = jsonData.getString("asset", "white");

		GameAsset<Texture> defaultValue = RuntimeContext.getInstance().AssetRepository.getAssetForIdentifier(assetIdentifier, GameAssetType.SPRITE);

		GameAsset<Texture> asset = GameResourceOwner.readAsset(json, jsonData);
		if (asset != null) {
			if (asset.isBroken()) {
				asset = defaultValue;
			}
			setGameAsset(asset);
		} else {
			setGameAsset(defaultValue);
		}
	}

	public void setToDefault () {
		GameAsset<Texture> defaultValue = RuntimeContext.getInstance().AssetRepository.getAssetForIdentifier("white", GameAssetType.SPRITE);
		setGameAsset(defaultValue);
	}

	@Override
	public void onUpdate () {
		if (asset != null && !asset.isBroken()) {
			region = new TextureRegion(asset.getResource());
		}
	}

	@Override
	public void remove () {
		super.remove();
		if (asset != null) {
			asset.listeners.removeValue(this, true);
		}
	}

	@Override
	public GameAssetType getGameAssetType () {
		return GameAssetType.SPRITE;
	}

	@Override
	public GameAsset<Texture> getGameResource () {
		return asset;
	}

	@Override
	public void setGameAsset (GameAsset<Texture> gameAsset) {
		if (this.asset != null) {
			//Remove from old game asset, it might be the same, but it may also have changed
			this.asset.listeners.removeValue(this, true);
		}

		this.asset = gameAsset;
		asset.listeners.add(this);

		if (asset != null && !asset.isBroken()) {
			region = new TextureRegion(asset.getResource());
		}
	}

	public TextureRegion getTextureRegion () {
		return region;
	}

}
