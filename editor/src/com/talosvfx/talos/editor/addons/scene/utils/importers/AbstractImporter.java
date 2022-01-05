package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public abstract class AbstractImporter {

    public abstract FileHandle importAsset(FileHandle fileHandle);
    public abstract void makeInstance(FileHandle asset, GameObject parent);


    public FileHandle importAssetFile (FileHandle handle) {
        String projectPath = SceneEditorAddon.get().workspace.getProjectPath();

        if (handle.path().startsWith(projectPath)) {
            FileHandle destination = Gdx.files.absolute(handle.parent().path() + File.separator + handle.name());
            createMetadataFor(destination);
            return handle;
        } else {
            FileHandle projectDir = Gdx.files.absolute(projectPath);
            FileHandle assetsDir = Gdx.files.absolute(projectDir.path() + File.separator + "assets");
            FileHandle destination = Gdx.files.absolute(assetsDir.path() + File.separator + handle.name());

            handle.copyTo(destination);

            createMetadataFor(destination);

            return destination;
        }
    }

    public abstract FileHandle createMetadataFor (FileHandle handle);

    public FileHandle renameAsset (FileHandle assetHandle, String name) {
        FileHandle destination = Gdx.files.absolute(assetHandle.parent() + File.separator + name);

        assetHandle.file().renameTo(destination.file());

        return destination;
    }
}
