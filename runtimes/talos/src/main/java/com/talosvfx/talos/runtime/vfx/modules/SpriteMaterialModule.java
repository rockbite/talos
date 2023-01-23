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

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.vfx.ParticleEmitterDescriptor;
import com.talosvfx.talos.runtime.vfx.assets.AssetProvider;
import com.talosvfx.talos.runtime.vfx.render.drawables.TextureRegionDrawable;
import com.talosvfx.talos.runtime.vfx.values.DrawableValue;
import com.talosvfx.talos.runtime.vfx.values.ModuleValue;

public class SpriteMaterialModule extends MaterialModule {

	private DrawableValue userDrawable;

	public String assetIdentifier = "white";

	private ModuleValue<SpriteMaterialModule> moduleOutput;

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
		Sprite asset = assetProvider.findAsset(assetIdentifier, Sprite.class);
		userDrawable.setDrawable(new TextureRegionDrawable(asset));
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


}
