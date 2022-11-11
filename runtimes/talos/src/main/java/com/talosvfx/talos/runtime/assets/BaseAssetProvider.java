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

package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;

public abstract class BaseAssetProvider implements AssetProvider {

	private ObjectMap<Class, AssetHandler> assetHandlers = new ObjectMap<>();

	public interface AssetHandler<T> {
		T findAsset (String assetName);
	}

	public BaseAssetProvider () {
	}

	public <T> void setAssetHandler (Class<T> clazz, AssetHandler<T> assetHandler) {
		assetHandlers.put(clazz, assetHandler);
	}

	@SuppressWarnings("unchecked")
	private <T> AssetHandler<T> getAssetHandler (Class<T> clazz) {
		final AssetHandler<T> assetHandler = assetHandlers.get(clazz);
		if (assetHandler == null) throw new GdxRuntimeException("No asset handler found for type: " + clazz.getName());
		return assetHandler;
	}

	@Override
	public <T> T findAsset (String assetName, Class<T> clazz) {
		return getAssetHandler(clazz).findAsset(assetName);
	}

}
