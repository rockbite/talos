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

package com.talosvfx.talos.editor.serialization;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.wrappers.ModuleWrapper;
import com.talosvfx.talos.runtime.vfx.serialization.ConnectionData;

public class EmitterData implements Json.Serializable {

	public String name;
	public int sortPosition;
	public boolean isMuted;
	public Array<ModuleWrapper> modules = new Array<>();
	public Array<ConnectionData> connections = new Array<>();
	public Array<GroupData> groups = new Array();

	public EmitterData () {

	}


	@Override
	public void write (Json json) {
		json.writeValue("name", name);
		json.writeValue("sortPosition", sortPosition);
		json.writeValue("isMuted", isMuted);
		json.writeValue("modules", modules);
		json.writeValue("connections", connections);
		json.writeValue("groups", groups);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		String talosIdentifier = jsonData.getString("talosIdentifier", "default");

		name = jsonData.getString("name");
		sortPosition = jsonData.getInt("sortPosition", 0);
		isMuted = jsonData.getBoolean("isMuted", false);

		JsonValue modlesJson = jsonData.get("modules");
		for (int i = 0; i < modlesJson.size; i++) {
			JsonValue moduleJson = modlesJson.get(i);
			moduleJson.addChild("talosIdentifier", new JsonValue(talosIdentifier));
			modules.add(json.readValue(ModuleWrapper.class, moduleJson));
		}

		connections = json.readValue("connections", Array.class, ConnectionData.class, jsonData);

		if (jsonData.has("groups")) {
			groups = json.readValue("groups", Array.class, GroupData.class, jsonData);
		}
	}
}
