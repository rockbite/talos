package com.talosvfx.talos.editor.addons.uieditor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.project.IProject;

public class UIProject implements IProject {

    private UIAddon uiAddon;

    public UIProject(UIAddon uiAddon) {
        this.uiAddon = uiAddon;
    }

    @Override
    public void loadProject (String data) {
        Json json = new Json();
        JsonValue jsonValue = new JsonReader().parse(data);
    }

    @Override
    public String getProjectString () {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        String data = "";
        //data = json.prettyPrint();
        return data;
    }

    @Override
    public void resetToNew () {

    }

    @Override
    public String getExtension () {
        return ".ui";
    }

    @Override
    public String getExportExtension () {
        return ".json";
    }

    @Override
    public String getProjectNameTemplate () {
        return "UIProject";
    }

    @Override
    public void initUIContent () {
        uiAddon.initUIContent();
    }

    @Override
    public FileHandle findFileInDefaultPaths (String fileName) {
        return null;
    }

    @Override
    public Array<String> getSavedResourcePaths () {
        return null;
    }

    @Override
    public String exportProject () {
        return "";
    }
}
