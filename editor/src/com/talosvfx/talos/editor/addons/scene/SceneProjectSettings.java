package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class SceneProjectSettings implements Json.Serializable {

    public float cameraX, cameraY, cameraZoom = 1, directorySize = 0.72f;
    public String directoryPath = "";


    @Override
    public void write (Json json) {
        json.writeValue("cameraX", cameraX);
        json.writeValue("cameraY", cameraY);
        json.writeValue("cameraZoom", cameraZoom);
        json.writeValue("directorySize", directorySize);
        json.writeValue("directoryPath", directoryPath);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        cameraX = jsonData.getFloat("cameraX");
        cameraY = jsonData.getFloat("cameraY");
        cameraZoom = jsonData.getFloat("cameraZoom");
        directorySize = jsonData.getFloat("directorySize");
        directoryPath = jsonData.getString("directoryPath");
    }

    public void updateValues () {
//        SceneEditorWorkspace sceneEditorWorkspace = SceneEditorWorkspace.getInstance();
//
//        cameraX = sceneEditorWorkspace.getCameraPosX();
//        cameraY = sceneEditorWorkspace.getCameraPosY();
//        cameraZoom = sceneEditorWorkspace.getCameraZoom();
//        directoryPath = SceneEditorAddon.get().projectExplorer.getCurrentFolder().path();
//        directorySize = SceneEditorAddon.get().verticalSplitPane.getSplit();
    }
}
