package com.talosvfx.talos.editor.addons.bvb;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.dialogs.SettingsDialog;
import com.talosvfx.talos.editor.project.IProject;
import com.talosvfx.talos.editor.utils.FileUtils;

public class BvbProject implements IProject {

    BvBAddon bvBAddon;

    public BvbProject(BvBAddon addon) {
        bvBAddon = addon;
    }

    @Override
    public void loadProject (FileHandle projectFileHandle, String data, boolean fromMemory) {
        Json json = new Json();
        JsonValue jsonValue = new JsonReader().parse(data);
        bvBAddon.workspace.read(json, jsonValue);
    }

    @Override
    public String getProjectString(boolean toMemory) {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        String data = json.prettyPrint(bvBAddon.workspace);
        return data;
    }

    @Override
    public void resetToNew() {
        bvBAddon.workspace.cleanWorkspace();
    }

    @Override
    public String getExtension() {
        return ".bvb";
    }

    @Override
    public String getExportExtension() {
        return ".json";
    }

    @Override
    public String getProjectNameTemplate() {
        return "skeleton";
    }

    @Override
    public void initUIContent() {
        bvBAddon.initUIContent();
    }

    @Override
    public FileHandle findFileInDefaultPaths(String fileName) {
        //what are we looking for exactly?
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        String path = null;
        if(extension.equals("json")) {
            // looking for spine skeletal animation file right?
            path = TalosMain.Instance().Prefs().getString("bvbSpineJsonPath");
        } else if(extension.equals("p")) {
            // looking for particle export file maybe
            path = TalosMain.Instance().Prefs().getString("bvbParticlePath");
        } else if(extension.equals("atlas")) {
            // looking for an atlas file for your spine animation I am guessing
            path = TalosMain.Instance().Prefs().getString("bvbSpineAtlasPath");
        } else if(extension.equals("png")) {
            // looking for an atlas file for your spine animation I am guessing
            path = TalosMain.Instance().Prefs().getString(SettingsDialog.ASSET_PATH);
        } else {
            //uh well we're screwed I don't know where to look for this guy
            return null;
        }

        FileHandle handle = FileUtils.findFileRecursive(path, fileName, 10);

        return handle;
    }

    @Override
    public Array<String> getSavedResourcePaths () {
        return null;
    }

    @Override
    public String exportProject() {
        return bvBAddon.workspace.writeExport();
    }

    @Override
    public String getProjectTypeName () {
        return "BVB";
    }

    @Override
    public boolean requiresWorkspaceLocation () {
        return false;
    }

    @Override
    public void createWorkspaceEnvironment (String path, String name) {

    }
}
