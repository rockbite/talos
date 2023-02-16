package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.runtime.assets.GameAssetType;

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

    public static Drawable getFileIconByType (String extension) {
        if (GameAssetType.SCENE.getExtensions().contains(extension)) {
            return SharedResources.skin.getDrawable("ic-new-scene");
        }
        if (GameAssetType.TILE_PALETTE.getExtensions().contains(extension)) {
            return SharedResources.skin.getDrawable("ic-pallete");
        }
        if (GameAssetType.PREFAB.getExtensions().contains(extension)) {
            return SharedResources.skin.getDrawable("ic-prefab");
        }
        if (GameAssetType.ROUTINE.getExtensions().contains(extension)) {
            return SharedResources.skin.getDrawable("ic-routine");
        }
        if (GameAssetType.SCRIPT.getExtensions().contains(extension)) {
            return SharedResources.skin.getDrawable("ic-script");
        }
        if (GameAssetType.VFX.getExtensions().contains(extension)) {
            return SharedResources.skin.getDrawable("ic-vfx");
        }
        // default
        return SharedResources.skin.getDrawable("ic-file-big");
    }
}
