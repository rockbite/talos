package com.rockbite.tools.talos.editor.serialization;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.wrappers.WrapperRegistry;
import com.rockbite.tools.talos.runtime.ParticleEmitterDescriptor;

public class ProjectSerializer {

    public ProjectSerializer () {
    }

    public ProjectData read (FileHandle fileHandle) {
        if(!fileHandle.exists()) return null;
        Json json = new Json();
        ParticleEmitterDescriptor.registerModules();
        for (Class clazz: WrapperRegistry.map.values()) {
            json.addClassTag(clazz.getSimpleName(), clazz);
        }
        for (Class clazz: ParticleEmitterDescriptor.registeredModules) {
            json.addClassTag(clazz.getSimpleName(), clazz);
        }
        return json.fromJson(ProjectData.class, fileHandle);

    }

    public void write (FileHandle fileHandle, ProjectData projectData) {
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
        fileHandle.writeString(data, false);
    }

    public void writeExport(FileHandle fileHandle, ExportData exportData) {
        Json json = new Json();
        ParticleEmitterDescriptor.registerModules();
        for (Class clazz: ParticleEmitterDescriptor.registeredModules) {
            json.addClassTag(clazz.getSimpleName(), clazz);
        }

        json.setOutputType(JsonWriter.OutputType.json);
        String data = json.prettyPrint(exportData);
        fileHandle.writeString(data, false);
    }
}
