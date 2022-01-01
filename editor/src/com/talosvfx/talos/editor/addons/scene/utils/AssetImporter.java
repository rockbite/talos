package com.talosvfx.talos.editor.addons.scene.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.utils.importers.SpriteImporter;
import com.talosvfx.talos.editor.addons.scene.utils.importers.TlsImporter;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.TlsMetadata;
import com.talosvfx.talos.editor.project.FileTracker;

import java.io.File;

public class AssetImporter {

    public FileTracker.Tracker assetTracker;

    private enum AssetType {
        SPRITE,
        TLS
    }

    private static ObjectMap<AssetType, Class<? extends AMetadata>> metadataClass;

    public AssetImporter() {
        assetTracker = handle -> assetUpdated(handle);

        if(metadataClass == null) {
            metadataClass = new ObjectMap<>();;
            metadataClass.put(AssetType.SPRITE, SpriteMetadata.class);
            metadataClass.put(AssetType.TLS, TlsMetadata.class);
        }
    }

    private void assetUpdated (FileHandle handle) {
        SceneEditorAddon.get().workspace.updateAsset(handle); //todo: maybe instead worth using events
    }

    public boolean attemptToImport (FileHandle handle) {
        FileHandle importedAsset = null;
        if(handle.extension().equals("png")) {
            importedAsset = SpriteImporter.run(handle);
        } else if(handle.extension().equals("tls")) {
            importedAsset = TlsImporter.run(handle);
        }

        if(importedAsset != null) {
            String projectPath = SceneEditorAddon.get().workspace.getProjectPath();
            SceneEditorAddon.get().projectExplorer.loadDirectoryTree(projectPath);
            SceneEditorAddon.get().projectExplorer.expand(importedAsset.path());

            TalosMain.Instance().ProjectController().saveProject();
        }

        return importedAsset != null;
    }

    public static FileHandle importAssetFile (FileHandle handle) {
        String projectPath = SceneEditorAddon.get().workspace.getProjectPath();

        FileHandle projectDir = Gdx.files.absolute(projectPath);
        FileHandle assetsDir = Gdx.files.absolute(projectDir.path() + File.separator + "assets");
        FileHandle destination = Gdx.files.absolute(assetsDir.path() + File.separator + handle.name());
        handle.copyTo(destination);

        // create metadata
        // todo: refactor this later
        if(handle.extension().equals("png")) {
            createMetadataFor(destination, AssetType.SPRITE);
        } else if(handle.extension().equals("tls")) {
            createMetadataFor(destination, AssetType.TLS);
        }

        return destination;
    }

    private static void createMetadataFor (FileHandle handle, AssetType type) {
        Class<? extends AMetadata> clazz = metadataClass.get(type);

        try {
            AMetadata aMetadata = ClassReflection.newInstance(clazz);
            FileHandle metadataHandle = getMetadataHandleFor(handle);
            saveMetadata(metadataHandle, aMetadata);
        } catch (ReflectionException e) {

        }
    }

    public static void saveMetadata (FileHandle handle, AMetadata aMetadata) {
        Json json = new Json();
        String data = json.toJson(aMetadata);
        handle.writeString(data, false);
    }

    public static FileHandle renameAsset (FileHandle assetHandle, String name) {
        FileHandle destination = Gdx.files.absolute(assetHandle.parent() + File.separator + name);
        assetHandle.moveTo(destination);

        return destination;
    }

    public static <T extends AMetadata> T readMetadata (FileHandle handle, Class<? extends T> clazz) {
        String data = handle.readString();
        Json json = new Json();
        T object = json.fromJson(clazz, data);
        return object;
    }

    public static FileHandle getMetadataHandleFor (FileHandle handle) {
        FileHandle metadataHandle = Gdx.files.absolute(handle.parent().path() + File.separator + handle.nameWithoutExtension() + ".meta");
        return metadataHandle;
    }

    public static FileHandle getMetadataHandleFor (String assetPath) {
        FileHandle handle = Gdx.files.absolute(assetPath);
        FileHandle metadataHandle = Gdx.files.absolute(handle.parent().path() + File.separator + handle.nameWithoutExtension() + ".meta");
        return metadataHandle;
    }

    public static FileHandle makeSimilar (FileHandle fileHandle, String extension) {
        String path = fileHandle.parent().path() + File.separator + fileHandle.nameWithoutExtension() + "." + extension;
        return Gdx.files.absolute(path);
    }
}
