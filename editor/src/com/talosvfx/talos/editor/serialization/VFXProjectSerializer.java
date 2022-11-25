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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.assets.TalosAssetProvider;
import com.talosvfx.talos.editor.project2.TalosVFXUtils;
import com.talosvfx.talos.editor.wrappers.WrapperRegistry;
import com.talosvfx.talos.runtime.ParticleEmitterDescriptor;
import com.talosvfx.talos.runtime.serialization.ExportData;

public class VFXProjectSerializer {

    public VFXProjectSerializer () {
    }

    /**
     * Very naughty
     * @param data
     */
    public void prereadhack (String data) {
        JsonReader jsonReader = new JsonReader();
        final JsonValue parse = jsonReader.parse(data);
        final JsonValue metaData = parse.get("metaData");
        if(metaData != null) {
            final JsonValue resourcePaths = metaData.get("resourcePaths");
            if (resourcePaths != null) {
                final TalosAssetProvider projectAssetProvider = TalosVFXUtils.talosAssetProvider;
                for (JsonValue resourcePath : resourcePaths) {
                    projectAssetProvider.addUnknownResource(resourcePath.asString());
                }
            }
        }
    }

    public static VFXProjectData readTalosTLSProject (FileHandle fileHandle) {
        if(!fileHandle.exists()) return null;
        return readTalosTLSProject(fileHandle.readString());
    }

    public static VFXProjectData readTalosTLSProject (String data) {
        Json json = new Json();
        ParticleEmitterDescriptor.registerModules();
        for (Class clazz: WrapperRegistry.map.values()) {
            json.addClassTag(clazz.getSimpleName(), clazz);
        }
        for (Class clazz: ParticleEmitterDescriptor.registeredModules) {
            json.addClassTag(clazz.getSimpleName(), clazz);
        }
        return json.fromJson(VFXProjectData.class, data);
    }

    public void write (FileHandle fileHandle, VFXProjectData VFXProjectData) {
        fileHandle.writeString(write(VFXProjectData), false);
    }

    public String write (VFXProjectData VFXProjectData) {
        Json json = new Json();
        ParticleEmitterDescriptor.registerModules();
        for (Class clazz: WrapperRegistry.map.values()) {
            json.addClassTag(clazz.getSimpleName(), clazz);
        }
        for (Class clazz: ParticleEmitterDescriptor.registeredModules) {
            json.addClassTag(clazz.getSimpleName(), clazz);
        }
        json.setOutputType(JsonWriter.OutputType.json);
        String data = json.prettyPrint(VFXProjectData);

        return data;
    }

    public static String writeTalosPExport (ExportData exportData) {
        Json json = new Json();
        ParticleEmitterDescriptor.registerModules();
        for (Class clazz: ParticleEmitterDescriptor.registeredModules) {
            json.addClassTag(clazz.getSimpleName(), clazz);
        }

        json.setOutputType(JsonWriter.OutputType.json);
        String data = json.toJson(exportData);

        return data;
    }


}
