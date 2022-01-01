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
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpriteMetadata;
import com.talosvfx.talos.editor.project.FileTracker;

import java.io.File;

public class AssetImporter {

    public FileTracker.Tracker assetTracker;

    private enum AssetType {
        SPRITE
    }

    private ObjectMap<AssetType, Class<? extends AMetadata>> metadataClass = new ObjectMap<>();

    public AssetImporter() {
        assetTracker = handle -> assetUpdated(handle);

        metadataClass.put(AssetType.SPRITE, SpriteMetadata.class);
    }

    private void assetUpdated (FileHandle handle) {
        SceneEditorAddon.get().workspace.updateAsset(handle); //todo: maybe instead worth using events
    }

    public boolean attemptToImport (FileHandle handle) {
        FileHandle importedAsset = null;
        if(handle.extension().equals("png")) {
            importedAsset = importSprite(handle);
        }

        if(importedAsset != null) {
            String projectPath = SceneEditorAddon.get().workspace.getProjectPath();
            SceneEditorAddon.get().projectExplorer.loadDirectoryTree(projectPath);
            SceneEditorAddon.get().projectExplorer.expand(importedAsset.path());

            TalosMain.Instance().ProjectController().saveProject();
        }

        return importedAsset != null;
    }

    public FileHandle importAssetFile (FileHandle handle) {
        String projectPath = SceneEditorAddon.get().workspace.getProjectPath();

        FileHandle projectDir = Gdx.files.absolute(projectPath);
        FileHandle assetsDir = Gdx.files.absolute(projectDir.path() + File.separator + "assets");
        FileHandle destination = Gdx.files.absolute(assetsDir.path() + File.separator + handle.name());
        handle.copyTo(destination);

        // create metadata
        createMetadataFor(destination, AssetType.SPRITE);

        return destination;
    }

    private void createMetadataFor (FileHandle handle, AssetType type) {
        Class<? extends AMetadata> clazz = metadataClass.get(type);

        try {
            AMetadata aMetadata = ClassReflection.newInstance(clazz);
            FileHandle metadataHandle = getMetadataHandleFor(handle);
            saveMetadata(metadataHandle, aMetadata);
        } catch (ReflectionException e) {

        }
    }

    private void saveMetadata (FileHandle handle, AMetadata aMetadata) {
        Json json = new Json();
        String data = json.toJson(aMetadata);
        handle.writeString(data, false);
    }

    private FileHandle importSprite(FileHandle fileHandle) {
        FileHandle importedAsset = importAssetFile(fileHandle);// this is now copied to our assets folder, and metadata created

        boolean was9slice = false;

        if(fileHandle.nameWithoutExtension().endsWith(".9")) {
            was9slice = true;
            // it's a nine slice, and needs metadata created accordingly
            FileHandle metadataHandle = getMetadataHandleFor(importedAsset);
            metadataHandle = renameAsset(metadataHandle, metadataHandle.nameWithoutExtension().replace(".9", "") + ".meta");
            importedAsset = renameAsset(importedAsset, importedAsset.nameWithoutExtension().replace(".9", "") + ".png");
            SpriteMetadata metadata = readMetadata(metadataHandle, SpriteMetadata.class);

            Pixmap pixmap = new Pixmap(importedAsset);
            int[] splits = ImportUtils.getSplits(pixmap);
            metadata.borderData = splits;

            saveMetadata(metadataHandle, metadata);

            Pixmap newPixmap = ImportUtils.cropImage(pixmap, 1, 1, pixmap.getWidth() - 1, pixmap.getHeight() - 1);
            PixmapIO.writePNG(importedAsset, newPixmap);

            pixmap.dispose();
            newPixmap.dispose();
        }

        SceneEditorWorkspace workspace = SceneEditorAddon.get().workspace;
        Vector2 sceneCords = workspace.getMouseCordsOnScene();
        GameObject gameObject = workspace.createSpriteObject(importedAsset, sceneCords);
        if(was9slice) {
            SpriteRendererComponent component = gameObject.getComponent(SpriteRendererComponent.class);
            component.renderMode = SpriteRendererComponent.RenderMode.sliced;
        }

        return importedAsset;
    }

    private FileHandle renameAsset (FileHandle assetHandle, String name) {
        FileHandle destination = Gdx.files.absolute(assetHandle.parent() + File.separator + name);
        assetHandle.moveTo(destination);

        return destination;
    }

    public <T extends AMetadata> T readMetadata (FileHandle handle, Class<? extends T> clazz) {
        String data = handle.readString();
        Json json = new Json();
        T object = json.fromJson(clazz, data);
        return object;
    }

    private FileHandle getMetadataHandleFor (FileHandle handle) {
        FileHandle metadataHandle = Gdx.files.absolute(handle.parent().path() + File.separator + handle.nameWithoutExtension() + ".meta");
        return metadataHandle;
    }

    public static FileHandle getMetadataHandleFor (String assetPath) {
        FileHandle handle = Gdx.files.absolute(assetPath);
        FileHandle metadataHandle = Gdx.files.absolute(handle.parent().path() + File.separator + handle.nameWithoutExtension() + ".meta");
        return metadataHandle;
    }
}
