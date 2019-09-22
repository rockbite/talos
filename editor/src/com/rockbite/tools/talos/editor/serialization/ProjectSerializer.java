package com.rockbite.tools.talos.editor.serialization;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.rockbite.tools.talos.editor.EmitterWrapper;
import com.rockbite.tools.talos.editor.NodeStage;
import com.rockbite.tools.talos.editor.widgets.ui.ModuleBoardWidget;
import com.rockbite.tools.talos.editor.wrappers.ModuleWrapper;

public class ProjectSerializer {

    public ProjectSerializer () {
    }

    public ProjectData read (FileHandle fileHandle) {
        if(!fileHandle.exists()) return null;

        Json json = new Json();
        return json.fromJson(ProjectData.class, fileHandle);

    }

    public void write (FileHandle fileHandle, ProjectData projectData) {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        String data = json.prettyPrint(projectData);
        fileHandle.writeString(data, false);
    }
}
