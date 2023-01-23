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

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class MaterialModule extends AbstractModule {

	public static final int MATERIAL_MODULE = 0;

	public MaterialModule () {

	}

	@Override
	protected void defineSlots() {
	}

	@Override
	public void processCustomValues () {

	}

	@Override
	public void write (Json json) {
		super.write(json);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
	}
}
