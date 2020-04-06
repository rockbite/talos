package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.File;

public class FileUtils {

    public static FileHandle findFileRecursive(String root, String fileName, int depthLeft) {
        if(depthLeft == 0) return null;

        FileHandle fileHandle = Gdx.files.absolute(root + File.separator + fileName);
        if(fileHandle.exists()) {
            return fileHandle;
        }

        FileHandle[] list = Gdx.files.absolute(root).list();
        if(list != null) {
            for(int i = 0; i < list.length; i++) {
                if(list[i].isDirectory()) {
                    FileHandle result = findFileRecursive(list[i].path(), fileName, depthLeft-1);
                    if(result != null && result.exists()) {
                        return result;
                    }
                }
            }
        }

        return null;
    }
}
