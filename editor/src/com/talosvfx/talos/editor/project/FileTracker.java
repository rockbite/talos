package com.talosvfx.talos.editor.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.widgets.ui.FileTab;

public class FileTracker {


    ObjectMap<FileTab, ObjectMap<FileHandle, FileEntry>> tabMaps = new ObjectMap<>();

    public FileTracker() {

    }

    public void addSavedResourcePathsFor (FileTab currentTab, Array<String> savedResourcePaths) {

        if(savedResourcePaths == null) return;

        final ObjectMap<FileHandle, FileEntry> entries = new ObjectMap<>();
        for (String savedResourcePath : savedResourcePaths) {
            FileHandle fileHandle = Gdx.files.absolute(savedResourcePath);
            FileEntry fileEntry = new FileEntry(fileHandle, new Tracker() {
                @Override
                public void updated (FileHandle handle) {

                }
            });
            entries.put(fileHandle, fileEntry);
        }

        tabMaps.put(currentTab, entries);
    }

    public void addTab (FileTab tab) {
        tabMaps.put(tab, new ObjectMap<>());
    }

    public FileHandle findFileByName(String name) {
        final FileTab currentTab = TalosMain.Instance().ProjectController().currentTab;

        if(tabMaps.get(currentTab) == null) return null;

        for(FileHandle handle: tabMaps.get(currentTab).keys()) {
            if(handle.name().equals(name) || handle.nameWithoutExtension().equals(name)) {
                if(handle.exists()) return handle;
            }
        }

        return null;
    }

    public class FileEntry {
        FileHandle fileHandle;
        Tracker callback;
        long lastModified;

        FileEntry(FileHandle fileHandle, Tracker callback) {
            this.fileHandle = fileHandle;
            this.callback = callback;
            if(fileHandle.exists()) {
                lastModified = fileHandle.lastModified();
            }
        }
    }

    public interface Tracker {
        void updated(FileHandle handle);
    }

    public ObjectMap<FileHandle, FileEntry> getCurrentTabFiles () {
        return tabMaps.get(TalosMain.Instance().ProjectController().currentTab);
    }


    public void trackFile(FileHandle fileHandle, Tracker tracker) {
        final FileTab currentTab = TalosMain.Instance().ProjectController().currentTab;

        if (!tabMaps.containsKey(currentTab)) {
            tabMaps.put(currentTab, new ObjectMap<>());
        }

        final ObjectMap<FileHandle, FileEntry> entries = tabMaps.get(currentTab);
        entries.put(fileHandle, new FileEntry(fileHandle, tracker));
    }

    public void update() {
        Array<FileHandle> filesToRemove = new Array<>();

        final FileTab currentTab = TalosMain.Instance().ProjectController().currentTab;

        for (ObjectMap.Entry<FileTab, ObjectMap<FileHandle, FileEntry>> tabMapEntry : tabMaps) {
            boolean isCurrentTab = tabMapEntry.key == currentTab;

            final ObjectMap<FileHandle, FileEntry> files = tabMapEntry.value;


            for (ObjectMap.Entry<FileHandle, FileEntry> entry : files) {
                final FileEntry fileEntry = entry.value;
                if(!fileEntry.fileHandle.exists()) {
                    filesToRemove.add(fileEntry.fileHandle);
                }
                if(fileEntry.lastModified < fileEntry.fileHandle.lastModified()) {
                    if (isCurrentTab) {
                        fileEntry.callback.updated(fileEntry.fileHandle);
                    }
                    fileEntry.lastModified = fileEntry.fileHandle.lastModified();
                }
            }


            for (FileHandle handle: filesToRemove) {
                files.remove(handle);
            }
        }


    }
}
