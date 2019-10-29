package com.rockbite.tools.talos.editor.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.widgets.ui.FileTab;

import java.io.File;

public class ProjectController {

    private String currentProjectPath = null;
    private String projectFileName = null;
    public FileTab currentTab;
    private ObjectMap<String, String> fileCache = new ObjectMap<>();
    private ObjectMap<String, String> pathCache = new ObjectMap<>();
    private boolean loading = false;

    IProject currentProject;

    public static TalosProject TLS = new TalosProject();

    public ProjectController() {
        currentProject = TLS;
    }

    public void loadProject (FileHandle projectFileHandle) {
        if (projectFileHandle.exists()) {
            if(currentTab != null) {
                saveProjectToCache(projectFileName);
            }
            currentProjectPath = projectFileHandle.path();
            projectFileName = projectFileHandle.name();
            loading = true;
            currentProject.loadProject(projectFileHandle.readString());
            loading = false;

            currentTab = new FileTab(projectFileName, currentProject);
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

    public void newProject (IProject project) {
        if(currentTab != null) {
            saveProjectToCache(projectFileName);
        }

        String newName = getNewFilename(project);
        FileTab tab = new FileTab(newName, project);
        TalosMain.Instance().UIStage().tabbedPane.add(tab);

        currentProject.resetToNew();
        currentProjectPath = null;
    }

    public String getNewFilename(IProject project) {
        int index = 1;
        String name = project.getProjectNameTemplate() + index + project.getExtension();
        while (fileCache.containsKey(name)) {
            index++;
            name = project.getProjectNameTemplate() + index + project.getExtension();
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
        }
        if(fileCache.containsKey(fileName)) {
            currentProject = tab.getProjectType();
            getProjectFromCache(fileName);
        }

        projectFileName = fileName;
        currentTab = tab;
        currentProject = currentTab.getProjectType();
        if(tab.getProjectType() == TLS) {
            TalosMain.Instance().UIStage().swapToTalosContent();
        } else {
            currentProject.initUIContent();
        }
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
        if(project.equals(TLS)) {
            TalosMain.Instance().UIStage().swapToTalosContent();
        }
    }

    public IProject getProject() {
        return currentProject;
    }

    public FileHandle findFile(FileHandle initialFile) {
        String fileName = initialFile.name();

        // local is priority, then the path, then the default lookup
        // do we currently have project loaded?
        if(currentProjectPath != null) {
            // we can look for local file then
            FileHandle currentProjectHandle = Gdx.files.absolute(currentProjectPath);
            if(currentProjectHandle.exists()) {
                String localPath = currentProjectHandle.parent().path() + File.separator + fileName;
                FileHandle localTry = Gdx.files.absolute(localPath);
                if(localTry.exists()) {
                    return localTry;
                }
            }
        }

        //Maybe the absolute path was a better ideas
        if(initialFile.exists()) return initialFile;

        //oh crap it's nowhere to be found, default path to the rescue!
        FileHandle lastHopeHandle = currentProject.findFileInDefaultPaths(fileName);
        if(lastHopeHandle != null && lastHopeHandle.exists()) {
            return lastHopeHandle;
        }

        // well we did all we could. seppuku is imminent
        return null;
    }
}
