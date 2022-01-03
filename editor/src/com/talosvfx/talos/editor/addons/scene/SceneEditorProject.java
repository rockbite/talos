package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.events.ProjectOpened;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.Scene;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project.IProject;

import java.io.File;

public class SceneEditorProject implements IProject {

    public SceneEditorAddon sceneEditorAddon;

    public SceneEditorProject (SceneEditorAddon sceneEditorAddon) {
        this.sceneEditorAddon = sceneEditorAddon;
    }

    @Override
    public void loadProject (FileHandle projectFileHandle, String data, boolean fromMemory) {
        Json json = new Json();
        JsonValue jsonValue = new JsonReader().parse(data);

        sceneEditorAddon.workspace.readProjectPath(projectFileHandle, jsonValue);

        sceneEditorAddon.workspace.loadFromData(json, jsonValue, fromMemory);
    }

    @Override
    public String getProjectString (boolean toMemory) {
        return sceneEditorAddon.workspace.saveData(toMemory);
    }

    @Override
    public void resetToNew () {
        sceneEditorAddon.workspace.cleanWorkspace();
    }

    @Override
    public String getExtension () {
        return ".tse";
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
        FileHandle assetsDir = Gdx.files.absolute(projectDir.path() + File.separator + "assets");
        assetsDir.mkdirs();

        // now save the project here
        sceneEditorAddon.workspace.setProjectPath(projectDir.path());
        FileHandle projectFile = Gdx.files.absolute(projectDir.path() + File.separator + name + SceneEditorAddon.SE.getExtension());

        // create new scene
        Scene mainScene = new Scene(projectDir.path() + File.separator + "scenes" + File.separator + "main_scene.scn");
        mainScene.save();
        sceneEditorAddon.workspace.addScene(mainScene);
        sceneEditorAddon.workspace.openScene(mainScene);

        // now create main camera
        sceneEditorAddon.workspace.createObjectByTypeName("camera", new Vector2(0, 0), null);

        TalosMain.Instance().ProjectController().saveProject(projectFile);

        sceneEditorAddon.workspace.reloadProjectExplorer();
        sceneEditorAddon.projectExplorer.select(mainScene.path);

        Notifications.fireEvent(Notifications.obtainEvent(ProjectOpened.class));
    }
}
