package com.rockbite.tools.talos.editor.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.widgets.ui.FileTab;

public class ProjectController {

    private String currentProjectPath = null;
    private String projectFileName = null;
    public FileTab currentTab;
    private ObjectMap<String, String> fileCache = new ObjectMap<>();
    private ObjectMap<String, String> pathCache = new ObjectMap<>();
    private boolean loading = false;

    IProject currentProject;

    public void loadProject (FileHandle projectFileHandle) {
        if (projectFileHandle.exists()) {
            currentProjectPath = projectFileHandle.path();
            projectFileName = projectFileHandle.name();

            loading = true;
            currentProject.loadProject(projectFileHandle.readString());
            loading = false;

            currentTab = new FileTab(projectFileName);
            TalosMain.Instance().UIStage().tabbedPane.add(currentTab);
        } else {
            //error handle
        }
    }

    private void saveProjectToCache(String projectFileName) {
        fileCache.put(projectFileName, currentProject.getProjectString());
        pathCache.put(projectFileName, currentProjectPath);
    }

    private void getProjectFromCache(String projectFileName) {
        loading = true;
        currentProject.loadProject(fileCache.get(projectFileName));
        loading = false;
        currentProjectPath = pathCache.get(projectFileName);
    }

    public void saveProject (FileHandle destination) {
        String data = currentProject.getProjectString();
        destination.writeString(data, false);
        currentTab.setDirty(false);
        currentProjectPath = destination.path();
        projectFileName = destination.name();

        if(!currentTab.getFileName().equals(projectFileName)) {
            clearCache(currentTab.getFileName());
            currentTab.setFileName(projectFileName);
            TalosMain.Instance().UIStage().tabbedPane.updateTabTitle(currentTab);
            fileCache.put(projectFileName, data);
        }
    }

    public void saveProject() {
        if(isBoundToFile()) {
            FileHandle handle = Gdx.files.absolute(currentProjectPath);
            saveProject(handle);
        }
    }

    public void newProject () {
        if(currentTab != null) {
            saveProjectToCache(projectFileName);
        }

        String newName = getNewFilename();
        FileTab tab = new FileTab(newName);
        TalosMain.Instance().UIStage().tabbedPane.add(tab);

        currentProject.resetToNew();
        currentProjectPath = null;
    }

    public String getNewFilename() {
        int index = 1;
        String name = "effect" + index + ".tls";
        while (fileCache.containsKey(name)) {
            index++;
            name = "effect" + index + ".tls";
        }

        return name;
    }

    public boolean isBoundToFile() {
        return currentProjectPath != null;
    }

    public void unbindFromFile() {
        currentProjectPath = null;
    }

    public String getPath() {
        return currentProjectPath;
    }


    public void setDirty() {
        if(!loading) {
            currentTab.setDirty(true);
        }
    }

    public void loadFromTab(FileTab tab) {
        String fileName = tab.getFileName();

        if(currentTab != null) {
            saveProjectToCache(projectFileName);
            if(fileCache.containsKey(fileName)) {
                getProjectFromCache(fileName);
            }
        }

        projectFileName = fileName;
        currentTab = tab;
    }

    public void removeTab(FileTab tab) {
        String fileName = tab.getFileName();
        clearCache(fileName);
        currentTab = null;
    }

    public void clearCache(String fileName) {
        pathCache.remove(fileName);
        fileCache.remove(fileName);
    }

    public void setProject(IProject project) {
        currentProject = project;
    }

    public IProject getProject() {
        return currentProject;
    }
}
