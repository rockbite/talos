package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class FileTracker {

    ObjectMap<FileHandle, FileEntry> files = new ObjectMap<>();

    public FileTracker() {

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

    public void trackFile(FileHandle fileHandle, Tracker tracker) {
        FileEntry fileEntry = new FileEntry(fileHandle, tracker);
        files.put(fileHandle, fileEntry);
    }

    public void update() {
        Array<FileHandle> filesToRemove = new Array<>();

        for(FileEntry entry: files.values()) {
            if(!entry.fileHandle.exists()) {
                filesToRemove.add(entry.fileHandle);
            }
            if(entry.lastModified < entry.fileHandle.lastModified()) {
                entry.callback.updated(entry.fileHandle);
                entry.lastModified = entry.fileHandle.lastModified();
            }
        }

        for(FileHandle handle: filesToRemove) {
            files.remove(handle);
        }
    }
}
