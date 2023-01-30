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
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.vfx.ParticleEmitterDescriptor;
import com.talosvfx.talos.runtime.vfx.assets.AssetProvider;
import com.talosvfx.talos.runtime.vfx.render.drawables.TextureRegionDrawable;
import com.talosvfx.talos.runtime.vfx.values.DrawableValue;
import com.talosvfx.talos.runtime.vfx.values.ModuleValue;
import org.w3c.dom.Text;

public class SpriteMaterialModule extends MaterialModule implements GameAsset.GameAssetUpdateListener {

	private DrawableValue userDrawable;

	public String assetIdentifier = "white";

	private ModuleValue<SpriteMaterialModule> moduleOutput;

	private GameAsset<Texture> gameAsset;

	@Override
	protected void defineSlots() {
		moduleOutput = new ModuleValue<>();
		moduleOutput.setModule(this);

		userDrawable = new DrawableValue();
		userDrawable.setEmpty(true);

		createOutputSlot(MATERIAL_MODULE, moduleOutput);
	}

	public DrawableValue getDrawableValue () {
		return userDrawable;
	}

	@Override
	public void processCustomValues () {
	}

	public void setAsset (String identifier) {
		this.assetIdentifier = identifier;
		final AssetProvider assetProvider = graph.getEffectDescriptor().getAssetProvider();
		GameAsset<?> asset = assetProvider.findGameAsset(assetIdentifier, Sprite.class);
		asset.listeners.add(this);
		this.gameAsset = (GameAsset<Texture>) asset;
		userDrawable.setDrawable(new TextureRegionDrawable(new Sprite(gameAsset.getResource())));
	}

	@Override
	public void setModuleGraph(ParticleEmitterDescriptor graph) {
		super.setModuleGraph(graph);
		setAsset(assetIdentifier);
	}

	@Override
	public void write (Json json) {
		super.write(json);
		json.writeValue("asset", assetIdentifier);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		assetIdentifier = jsonData.getString("asset", "white");


	}


	@Override
	public void onUpdate() {
		if (gameAsset != null && !gameAsset.isBroken()) {
			userDrawable.getDrawable().getTextureRegion().setTexture(gameAsset.getResource());
		}
	}

	@Override
	public void remove() {
		super.remove();
		if(gameAsset!=null){
			gameAsset.listeners.removeValue(this, true);
		}
	}
}
