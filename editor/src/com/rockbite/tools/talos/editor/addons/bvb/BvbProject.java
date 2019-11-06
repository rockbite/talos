package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.project.IProject;

import java.io.File;

public class BvbProject implements IProject {

    BvBAddon bvBAddon;

    public BvbProject(BvBAddon addon) {
        bvBAddon = addon;
    }

    @Override
    public void loadProject(String data) {
        Json json = new Json();
        JsonValue jsonValue = new JsonReader().parse(data);
        bvBAddon.workspace.read(json, jsonValue);
    }

    @Override
    public String getProjectString() {
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
        } else {
            //uh well we're screwed I don't know where to look for this guy
            return null;
        }

        FileHandle handle = Gdx.files.absolute(path + File.separator + fileName);
        return handle;
    }

    @Override
    public Array<String> getSavedResourcePaths () {
        return new Array<>();
    }

    @Override
    public String exportProject() {
        return bvBAddon.workspace.writeExport();
    }
}
