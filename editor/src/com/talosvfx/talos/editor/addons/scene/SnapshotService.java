package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.utils.ObjectMap;

public class SnapshotService {

    private ObjectMap<String, ObjectMap<String, String>> data = new ObjectMap<>();

    public String getSnapshot (String changeVersion, String path) {
        if(!data.containsKey(changeVersion)) {
            return null;
        }

        return data.get(changeVersion).get(path);
    }

    public void saveSnapshot (String changeVersion, String scenePath, String sceneData) {
        if(!data.containsKey(changeVersion)) {
            ObjectMap<String, String> fileMap = new ObjectMap<>();
            data.put(changeVersion, fileMap);
        }

        data.get(changeVersion).put(scenePath, sceneData);
    }
}
