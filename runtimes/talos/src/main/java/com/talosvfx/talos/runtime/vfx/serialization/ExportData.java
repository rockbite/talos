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

package com.talosvfx.talos.runtime.vfx.serialization;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.StringBuilder;
import com.talosvfx.talos.runtime.assets.GameResourceOwner;
import com.talosvfx.talos.runtime.utils.Supplier;
import com.talosvfx.talos.runtime.vfx.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.vfx.modules.AbstractModule;


public class ExportData extends BaseVFXProjectData implements Json.Serializable {

    public ExportData () {
        super();
    }

    public Array<EmitterExportData> emitters = new Array<>();

    public ExportMetadata metadata = new ExportMetadata();
    private transient ParticleEffectDescriptor particleEffectDescriptorLoaded;

    private transient Supplier<ParticleEffectDescriptor> descriptorSupplier = new Supplier<ParticleEffectDescriptor>() {
        @Override
        public ParticleEffectDescriptor get () {
           return particleEffectDescriptorLoaded;
        }
    };

    @Override
    public Supplier<ParticleEffectDescriptor> getDescriptorSupplier () {
        return descriptorSupplier;
    }

    public void setDescriptorLoaded (ParticleEffectDescriptor particleEffectDescriptor) {
        this.particleEffectDescriptorLoaded = particleEffectDescriptor;
    }

    @Override
    public void write (Json json) {
        json.writeValue("metadata", metadata);
        json.writeValue("emitters", emitters);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        String talosIdentifier = GameResourceOwner.readTalosIdentifier(jsonData);
        metadata = json.readValue("metadata", ExportMetadata.class, jsonData);
        JsonValue emittersJsonValue = jsonData.get("emitters");
        for (int i = 0; i < emittersJsonValue.size; i++) {
            JsonValue emitterJsonValue = emittersJsonValue.get(i);
            emitterJsonValue.addChild("talosIdentifier", new JsonValue(talosIdentifier));
            emitters.add(json.readValue(EmitterExportData.class, emitterJsonValue));
        }
    }

    public static class ExportMetadata {
        public Array<String> resources = new Array<>();
        public String versionString;

        public ExportMetadata () {}
    }

    public static class EmitterExportData implements Json.Serializable {
        public String name;
        public Array<AbstractModule> modules = new Array<>();
        public Array<ConnectionData> connections = new Array<>();

        public EmitterExportData () {}

        @Override
        public String toString () {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(name);
            stringBuilder.append("\n");

            for (AbstractModule module : modules) {
                stringBuilder.append("\t");
                stringBuilder.append("ModuleID: ");
                stringBuilder.append(module.getIndex());
                stringBuilder.append("\n");
            }

            stringBuilder.append("\n");
            for (ConnectionData connection : connections) {
                stringBuilder.append("\t");
                stringBuilder.append("Connection: ");
                stringBuilder.append(connection);
                stringBuilder.append("\n");
            }

            return stringBuilder.toString();
        }

        @Override
        public void write (Json json) {
            json.writeValue("name", name);
            json.writeValue("modules", modules);
            json.writeValue("connections", connections);
        }

        @Override
        public void read (Json json, JsonValue jsonData) {
            String talosIdentifier = GameResourceOwner.readTalosIdentifier(jsonData);

            name = jsonData.getString("name");
            JsonValue modulesJsonValue = jsonData.get("modules");
            for (int i = 0; i < modulesJsonValue.size; i++) {
                JsonValue abstractModuleJson = modulesJsonValue.get(i);
                abstractModuleJson.addChild("talosIdentifier", new JsonValue(talosIdentifier));
                modules.add(json.readValue(AbstractModule.class, abstractModuleJson));
            }
            connections = json.readValue(Array.class, ConnectionData.class, jsonData.get("connections"));
        }
    }

}
