package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.project.IProject;

import java.io.File;

public class SceneEditorProject implements IProject {

    private SceneEditorAddon sceneEditorAddon;

    public SceneEditorProject (SceneEditorAddon sceneEditorAddon) {
        this.sceneEditorAddon = sceneEditorAddon;
    }

    @Override
    public void loadProject (String data) {
        Json json = new Json();
        JsonValue jsonValue = new JsonReader().parse(data);
        sceneEditorAddon.workspace.read(json, jsonValue);
    }

    @Override
    public String getProjectString () {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        String data = json.prettyPrint(sceneEditorAddon.workspace);
        return data;
    }

    @Override
    public void resetToNew () {
        sceneEditorAddon.workspace.cleanWorkspace();
    }

    @Override
    public String getExtension () {
        return ".tscn";
    }

    @Override
    public String getExportExtension () {
        return ".scn";
    }

    @Override
    public String getProjectNameTemplate () {
        return "scene";
    }

    @Override
    public void initUIContent () {
        sceneEditorAddon.initUIContent();
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
        return sceneEditorAddon.workspace.writeExport();
    }

    @Override
    public String getProjectTypeName () {
        return "Scene";
    }

    @Override
    public boolean requiresWorkspaceLocation () {
        return true;
    }

    @Override
    public void createWorkspaceEnvironment (String path, String name) {
        name.replace("/", "");
        name.replace("\\", "");
        FileHandle parent = Gdx.files.absolute(path);

        FileHandle projectDir = Gdx.files.absolute(parent.path() + File.separator + name);
        projectDir.mkdirs();

        // now save the project here
        FileHandle projectFile = Gdx.files.absolute(projectDir.path() + File.separator + name + SceneEditorAddon.SE.getExtension());
        TalosMain.Instance().ProjectController().saveProject(projectFile);
    }
}
