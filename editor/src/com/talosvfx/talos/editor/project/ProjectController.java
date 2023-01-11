package com.talosvfx.talos.editor.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.dialogs.NewProjectDialog;
import com.talosvfx.talos.editor.widgets.ui.FileTab;

import java.io.File;
import java.util.Comparator;

public class ProjectController {

    private String currentProjectPath = null;
    private String projectFileName = null;
    public FileTab currentTab;
    private ObjectMap<String, String> fileCache = new ObjectMap<>();
    private ObjectMap<String, String> pathCache = new ObjectMap<>();
    private ObjectMap<String, FileTab> tabCache = new ObjectMap<>();
    private ObjectMap<String, String> exporthPathCache = new ObjectMap<>();
    private boolean loading = false;

    IProject currentProject;

    private SnapshotTracker snapshotTracker;

    private boolean lastDirTracking = true;

    public ProjectController() {

        snapshotTracker = new SnapshotTracker();
    }

    public void loadProject (FileHandle projectFileHandle) {
        try {
            if (projectFileHandle.exists()) {
                FileTab prevTab = currentTab;
                boolean removingUnworthy = false;

                if (currentTab != null) {
                    if (currentTab.getProjectType() == currentProject && currentTab.isUnworthy()) {
                        removingUnworthy = true;
                        clearCache(currentTab.getFileName());
                    } else {
                        IProject tmp = currentProject;
                        currentProject = currentTab.getProjectType();
                        saveProjectToCache(projectFileName);
                        currentProject = tmp;
                    }
                }
                currentProjectPath = projectFileHandle.path();
                projectFileName = projectFileHandle.name();
                loading = true;
                currentTab = new FileTab(projectFileHandle, currentProject); // trackers need to know what current tab is
                String string = projectFileHandle.readString();
                currentProject.loadProject(projectFileHandle, string, false);
                snapshotTracker.reset(string);
                reportProjectFileInterraction(projectFileHandle);
                loading = false;

                if (lastDirTracking) {
                    TalosMain.Instance().Prefs().putString("lastOpen" + currentProject.getExtension(), projectFileHandle.parent().path());
                    TalosMain.Instance().Prefs().flush();
                }

                TalosMain.Instance().UIStage().tabbedPane.add(currentTab);

                final Array<String> savedResourcePaths = currentProject.getSavedResourcePaths();
                TalosMain.Instance().FileTracker().addSavedResourcePathsFor(currentTab, savedResourcePaths);

                if (removingUnworthy) {
                    safeRemoveTab(prevTab);
                }
            } else {
                //error handle
            }
        } catch (Exception e) {
            TalosMain.Instance().reportException(e);
        }
    }

    private void saveProjectToCache(String projectFileName) {
        try {
            fileCache.put(projectFileName, currentProject.getProjectString(true));
            pathCache.put(projectFileName, currentProjectPath);
        } catch (Exception e) {
            TalosMain.Instance().reportException(e);
        }
    }

    private void getProjectFromString(String string, boolean fromMemory) {
        try {
            loading = true;
            currentProject.loadProject(null, string, fromMemory);
        } catch (Exception e) {
            TalosMain.Instance().reportException(e);
        } finally {
            loading = false;
        }
    }

    public void saveProject (FileHandle destination) {
        try {
            String data = currentProject.getProjectString(false);
            destination.writeString(data, false);

            reportProjectFileInterraction(destination);

            TalosMain.Instance().Prefs().putString("lastSave" + currentProject.getExtension(), destination.parent().path());
            TalosMain.Instance().Prefs().flush();

            currentTab.setDirty(false);
            currentTab.setWorthy();
            currentProjectPath = destination.path();
            projectFileName = destination.name();

            if (!currentTab.getFileName().equals(projectFileName)) {
                clearCache(currentTab.getFileName());
                currentTab.setProjectFileHandle(destination);
                TalosMain.Instance().UIStage().tabbedPane.updateTabTitle(currentTab);
                fileCache.put(projectFileName, data);
            }
        } catch (Exception e) {
            TalosMain.Instance().reportException(e);
        }
    }

    public void saveProject() {
        if(isBoundToFile()) {
            FileHandle handle = Gdx.files.absolute(currentProjectPath);
            saveProject(handle);
        }
    }

    public void newProject (IProject project) {
        if(project.requiresWorkspaceLocation()) {
            String fileName = getNewFilename(project);
            String projectName = fileName.substring(0, fileName.indexOf("."));
            NewProjectDialog.show(project.getProjectTypeName(), projectName, new NewProjectDialog.NewProjectListener() {
                @Override
                public void create (String path, String name) {
                    createNewProjectTab(project, fileName);
                    project.createWorkspaceEnvironment(path, name);
                }
            });
        } else {
            String fileName = getNewFilename(project);
            createNewProjectTab(project, fileName);
        }
    }

    public void createNewProjectTab(IProject project, String fileName) {
        FileTab prevTab = currentTab;

        boolean removingUnworthy = false;

        if(currentTab != null) {
            if(currentTab.getProjectType() == project && currentTab.isUnworthy()) {
                removingUnworthy = true;
                clearCache(currentTab.getFileName());
            }  else {
                saveProjectToCache(projectFileName);
            }
        }

        final FileTab tab = new FileTab(Gdx.files.local(fileName), project);

        tab.setUnworthy(); // all new projects are unworthy, and will only become worthy when worked on
        TalosMain.Instance().UIStage().tabbedPane.add(tab);

        TalosMain.Instance().FileTracker().addTab(tab);

        currentProject.resetToNew();
        snapshotTracker.reset(currentProject.getProjectString(true));
        currentProjectPath = null;

        if(removingUnworthy) {
            safeRemoveTab(prevTab);
        }
    }

    /**
     * removes tab without listener crap
     */
    public void safeRemoveTab(FileTab tab) {
        FileTab tmp = currentTab;
        TalosMain.Instance().UIStage().tabbedPane.remove(tab);
        currentTab = tmp;
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

    public String getCurrentProjectPath () {
        return currentProjectPath;
    }


    public void setDirty() {
        if(!loading) {
            currentTab.setDirty(true);
            currentTab.setWorthy();

            // also add this as snapshot
            snapshotTracker.addSnapshot(getProjectString(true));
        }
    }

    private String getProjectString(boolean toMemory) {
        return currentProject.getProjectString(toMemory);
    }

    public void loadFromTab(FileTab tab) {
        String fileName = tab.getFileName();

        if (currentTab != null && currentTab != tab) {
            saveProjectToCache(projectFileName);
        }

        if (fileCache.containsKey(fileName)) {
            currentProject = tab.getProjectType();
            currentTab = tab;
            final FileHandle projectFileHandle = tab.getProjectFileHandle();
            final String fileData = fileCache.get(tab.getFileName());
            currentProject.loadProject(projectFileHandle, fileData, true);
        }

        projectFileName = fileName;
        currentTab = tab;
        currentProject = currentTab.getProjectType();
    }

    public void removeTab(FileTab tab) {
        String fileName = tab.getFileName();
        clearCache(fileName);
        if(tab == currentTab) {
            currentTab = null;
        }
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

    public FileHandle findFile(String path) {
        return findFile(Gdx.files.absolute(path));
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

    public void exportProject(FileHandle fileHandle) {
        exporthPathCache.put(projectFileName, fileHandle.path());

        String data = currentProject.exportProject();
        fileHandle.writeString(data, false);

        TalosMain.Instance().Prefs().putString("lastExport"+currentProject.getExtension(), fileHandle.parent().path());
        TalosMain.Instance().Prefs().flush();
    }

    public String getCurrentExportNameSuggestion() {
        if(currentTab != null) {
            String projectName = currentTab.getFileName();
            String exportExt = currentProject.getExportExtension();
            return projectName.substring(0, projectName.lastIndexOf(".")) + exportExt;
        }
        return "";
    }

    public String getLastDir(String action, IProject projectType) {
        String path = TalosMain.Instance().Prefs().getString("last" + action + projectType.getExtension());
        FileHandle handle = Gdx.files.absolute(path);
        if(handle.exists()) {
            return handle.path();
        }

        return "";
    }

    public String getExportPath() {
        return exporthPathCache.get(projectFileName);
    }

    public void lastDirTrackingDisable() {
        lastDirTracking = false;
    }

    public void lastDirTrackingEnable() {
        lastDirTracking = true;
    }

    public void undo() {
        boolean changed = snapshotTracker.moveBack();
        if (changed) {
            getProjectFromString(snapshotTracker.getCurrentSnapshot(), true);
        }
    }

    public void redo() {
        boolean changed = snapshotTracker.moveForward();
        if (changed) {
            getProjectFromString(snapshotTracker.getCurrentSnapshot(), true);
        }
    }

    public void closeCurrentTab() {
        safeRemoveTab(currentTab);
    }

    Comparator<RecentsEntry> recentsEntryComparator = new Comparator<RecentsEntry>() {
        @Override
        public int compare(RecentsEntry o1, RecentsEntry o2) {
            return (int) (o2.time - o1.time);
        }
    };

    public void reportProjectFileInterraction(FileHandle handle) {
        Preferences prefs = TalosMain.Instance().Prefs();
        String data = prefs.getString("recents");

        Recents recents = new Recents();

        //read
        Json json = new Json();
        try {
            if (data != null && !data.isEmpty()) {
                recents = json.fromJson(Recents.class, data);
            }
        } catch( Exception e) {
            e.printStackTrace();
        }
        RecentsEntry newEntry = new RecentsEntry(handle.path(), (int)TimeUtils.millis());
        recents.getRecents().removeValue(newEntry, false);
        recents.getRecents().add(newEntry);
        //sort
        recents.getRecents().sort(recentsEntryComparator);
        //write
        String result = json.toJson(recents);
        prefs.putString("recents", result);
        prefs.flush();
        updateRecentsList();
    }

    public Array<String> updateRecentsList() {
        Preferences prefs = TalosMain.Instance().Prefs();
        String data = prefs.getString("recents");

        Array<String> list = new Array<>();
        //read

        try {
            Json json = new Json();
            if (data != null && !data.isEmpty()) {
                Recents recents = json.fromJson(Recents.class, data);
                for (RecentsEntry recent : recents.getRecents()) {
                    list.add(recent.path);
                }
            }

            TalosMain.Instance().UIStage().Menu().updateRecentsList(list);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
