package com.talosvfx.talos.editor.addons.treedata;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.talosvfx.talos.editor.addons.shader.ShaderProject;
import com.talosvfx.talos.editor.addons.shader.workspace.ShaderNodeStage;
import com.talosvfx.talos.editor.addons.treedata.workspace.TreeDataNodeStage;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.project.IProject;

public class TreeDataProject implements IProject {

    private final TreeDataAddon treeDataAddon;

    public TreeDataProject (TreeDataAddon treeDataAddon) {
        this.treeDataAddon = treeDataAddon;
    }

    @Override
    public void loadProject (String data) {
        Json json = new Json();
        ProjectData projectData = json.fromJson(ProjectData.class, data);
        projectData.loadStage(treeDataAddon.nodeStage);
    }

    @Override
    public String getProjectString () {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);

        ProjectData projectData = new ProjectData();
        projectData.nodeStage = treeDataAddon.nodeStage;

        String data = json.prettyPrint(projectData);

        return data;
    }

    @Override
    public void resetToNew () {
        treeDataAddon.nodeStage.reset();
    }

    @Override
    public String getExtension () {
        return ".dcg"; // Dynamic Content Graph
    }

    @Override
    public String getExportExtension () {
        return ".xml";
    }

    @Override
    public String getProjectNameTemplate () {
        return "ContentFile";
    }

    @Override
    public void initUIContent () {
        treeDataAddon.initUIContent();
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
        return ((TreeDataNodeStage)(treeDataAddon.nodeStage)).getExportData();
    }

    private static class ProjectData implements Json.Serializable {

        public TreeDataProject.ProjectMetadata projectMetadata;
        public DynamicNodeStage nodeStage;

        private JsonValue nodeStageData;

        @Override
        public void write (Json json) {
            json.writeValue("metadata", projectMetadata);
            json.writeValue("nodes", nodeStage);
        }

        @Override
        public void read (Json json, JsonValue jsonValue) {
            projectMetadata = new TreeDataProject.ProjectMetadata();
            projectMetadata.read(json, jsonValue.get("metadata"));
            nodeStageData = jsonValue.get("nodes");
        }

        public void loadStage (DynamicNodeStage nodeStage) {
            Json json = new Json();
            nodeStage.read(json, nodeStageData);
        }
    }

    private static class ProjectMetadata implements Json.Serializable {

        @Override
        public void write (Json json) {

        }

        @Override
        public void read (Json json, JsonValue jsonValue) {

        }
    }
}
