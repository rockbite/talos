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

package com.rockbite.tools.talos.editor.serialization;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.rockbite.tools.talos.editor.wrappers.WrapperRegistry;
import com.rockbite.tools.talos.runtime.ParticleEmitterDescriptor;
import com.rockbite.tools.talos.runtime.serialization.ExportData;

public class ProjectSerializer {

    public ProjectSerializer () {
    }

    public ProjectData read (FileHandle fileHandle) {
        if(!fileHandle.exists()) return null;
        return read(fileHandle.readString());
    }

    public ProjectData read (String data) {
        Json json = new Json();
        ParticleEmitterDescriptor.registerModules();
        for (Class clazz: WrapperRegistry.map.values()) {
            json.addClassTag(clazz.getSimpleName(), clazz);
        }
        for (Class clazz: ParticleEmitterDescriptor.registeredModules) {
            json.addClassTag(clazz.getSimpleName(), clazz);
        }
        return json.fromJson(ProjectData.class, data);
    }

    public void write (FileHandle fileHandle, ProjectData projectData) {
        fileHandle.writeString(write(projectData), false);
    }

    public String write (ProjectData projectData) {
        Json json = new Json();
        ParticleEmitterDescriptor.registerModules();
        for (Class clazz: WrapperRegistry.map.values()) {
            json.addClassTag(clazz.getSimpleName(), clazz);
        }
        for (Class clazz: ParticleEmitterDescriptor.registeredModules) {
            json.addClassTag(clazz.getSimpleName(), clazz);
        }
        json.setOutputType(JsonWriter.OutputType.json);
        String data = json.prettyPrint(projectData);

        return data;
    }

    public String writeExport(ExportData exportData) {
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
