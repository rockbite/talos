package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;

public class SceneEditorWorkspace extends ViewportWidget implements Json.Serializable{

    private static SceneEditorWorkspace instance;
    private SceneEditorAddon sceneEditorAddon;

    public SceneEditorWorkspace() {
        setSkin(TalosMain.Instance().getSkin());
    }

    @Override
    public void write (Json json) {

    }

    @Override
    public void read (Json json, JsonValue jsonData) {

    }

    @Override
    public void drawContent (Batch batch, float parentAlpha) {

    }

    public void setAddon (SceneEditorAddon sceneEditorAddon) {
        this.sceneEditorAddon = sceneEditorAddon;
    }

    public static SceneEditorWorkspace getInstance() {
        if(instance == null) {
            instance = new SceneEditorWorkspace();
        }
        return instance;
    }

    public void cleanWorkspace () {

    }

    public String writeExport () {
        return "";
    }
}
