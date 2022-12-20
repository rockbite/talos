package com.talosvfx.talos.editor.addons.scene.apps.routines;

import com.badlogic.gdx.files.FileHandle;
import lombok.Getter;

public class RoutineData {

    @Getter
    public String jsonString;
    public static RoutineData readFrom(FileHandle handle) {
        RoutineData data = new RoutineData();
        data.jsonString = handle.readString();
        return data;
    }
}
