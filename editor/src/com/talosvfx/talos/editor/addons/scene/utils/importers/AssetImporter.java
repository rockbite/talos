package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.events.AssetPathChanged;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.Prefab;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.*;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project.FileTracker;
import com.talosvfx.talos.editor.project.ProjectController;
import com.talosvfx.talos.editor.utils.FileOpener;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

public class AssetImporter {

    public FileTracker.Tracker assetTracker;

    public enum AssetType {
        SPRITE,
        TLS,
        SPINE,
        ATLAS
    }

    private static ObjectMap<AssetType, AbstractImporter> importerMap = new ObjectMap();
    private static ObjectMap<String, Class<? extends AMetadata>> metadataMap = new ObjectMap();

    private static Json json = new Json();
    static {
        json.setOutputType(JsonWriter.OutputType.json);
    }

    public AssetImporter() {
        assetTracker = handle -> assetUpdated(handle);

        importerMap.put(AssetType.SPRITE, new SpriteImporter());
        importerMap.put(AssetType.TLS, new TlsImporter());
        importerMap.put(AssetType.SPINE, new SpineImporter());
        importerMap.put(AssetType.ATLAS, new AtlasImporter());

        metadataMap.put("png", SpriteMetadata.class);
        metadataMap.put("tls", TlsMetadata.class);
        metadataMap.put("p", TlsMetadata.class);
        metadataMap.put("atlas", AtlasMetadata.class);
        metadataMap.put("skel", SpineMetadata.class);
        metadataMap.put("prefab", PrefabMetadata.class);
    }

    public static FileHandle attemptToImport (FileHandle handle) {
        return attemptToImport(handle, false);
    }

    public static FileHandle attemptToImport (FileHandle handle, boolean andPlaceIt) {
        FileHandle destinationDir = SceneEditorAddon.get().projectExplorer.getCurrentFolder();

        AssetRepository.getInstance().copyRawAsset(handle, destinationDir);

        if (andPlaceIt) {
            System.out.println("Placing needs implementing again");
        }

        String projectPath = SceneEditorAddon.get().workspace.getProjectPath();
        SceneEditorAddon.get().projectExplorer.loadDirectoryTree(projectPath);
        SceneEditorAddon.get().projectExplorer.expand(destinationDir.path());
        SceneEditorAddon.get().projectExplorer.select(destinationDir.path());

        TalosMain.Instance().ProjectController().saveProject();

        return handle;
    }

    private void assetUpdated (FileHandle handle) {
        SceneEditorAddon.get().workspace.updateAsset(handle); //todo: maybe instead worth using events
    }

    // Check up on things, tidy up a bit
    public void housekeep (String projectPath) {
        FileHandle project = Gdx.files.absolute(projectPath);
        Array<FileHandle> tlsFiles = new Array<>();
        findInPath(project, "tls", tlsFiles);

        for(FileHandle tlsHandle: tlsFiles) {
            String checksum = checkSum(tlsHandle);
            TlsMetadata tlsMetadata = readMetadataFor(tlsHandle, TlsMetadata.class);
            if(!tlsMetadata.tlsChecksum.equals(checksum)) {
                ((TlsImporter)importerMap.get(AssetType.TLS)).exportTlsFile(tlsHandle);

                tlsMetadata.tlsChecksum = checksum;
                FileHandle metadataHandle = AssetImporter.getMetadataHandleFor(tlsHandle);
                AssetImporter.saveMetadata(metadataHandle, tlsMetadata);
            }
        }
    }

    public static AMetadata readMetadataFor (FileHandle assetHandle) {
        if(assetHandle.isDirectory()) {
            DirectoryMetadata directoryMetadata = new DirectoryMetadata();
            directoryMetadata.setFile(assetHandle);
            return directoryMetadata;
        }

        Class<? extends AMetadata> aClass = metadataMap.get(assetHandle.extension());
        return readMetadataFor(assetHandle, aClass);
    }

    public static <T extends AMetadata> T readMetadataFor (FileHandle assetHandle, Class<? extends T> clazz) {
        FileHandle handle = getMetadataHandleFor(assetHandle);
        T t = readMetadata(handle, clazz);

        if(t == null) {
            if(clazz != null) {
                try {
                    t = ClassReflection.newInstance(clazz);
                } catch (ReflectionException e) {
                    t = (T) new EmptyMetadata();
                }
            } else {
                t = (T) new EmptyMetadata();
            }
        }

        t.setFile(assetHandle);

        return t;
    }

    public static <T extends AMetadata> T readMetadata (FileHandle handle, Class<? extends T> clazz) {
        if(handle.exists()) {
            String data = handle.readString();
            T object = json.fromJson(clazz, data);
            return object;
        }

        return null;
    }

    public static <T extends AMetadata> T createEmptyMetadata (FileHandle handle, Class<? extends T> clazz) {
        try {
            T t = ClassReflection.newInstance(clazz);
            return t;
        } catch (ReflectionException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static FileHandle getMetadataHandleFor (FileHandle handle) {
        handle = get(handle.path());

        FileHandle metadataHandle = Gdx.files.absolute(handle.parent().path() + File.separator + handle.name() + ".meta");
        return metadataHandle;
    }

    public static FileHandle getMetadataHandleFor (String assetPath) {
        FileHandle handle = get(assetPath);
        FileHandle metadataHandle = Gdx.files.absolute(handle.parent().path() + File.separator + handle.name() + ".meta");
        return metadataHandle;
    }

    public static void createAssetInstance(FileHandle fileHandle, GameObject parent) {
        if(fileHandle.extension().equals("png")) {
            // check if non imported nine patch
            if(fileHandle.name().endsWith(".9.png")) {
                // import it
                attemptToImport(fileHandle);
            } else {
                importerMap.get(AssetType.SPRITE).makeInstance(fileHandle, parent);
            }
        }

        if(fileHandle.extension().equals("tls")) {
            importerMap.get(AssetType.TLS).makeInstance(fileHandle, parent);
        }
        if(fileHandle.extension().equals("p")) {
            importerMap.get(AssetType.TLS).makeInstance(fileHandle, parent);
        }

        if(fileHandle.extension().equals("skel")) {
            importerMap.get(AssetType.SPINE).makeInstance(fileHandle, parent);
        }

        if(fileHandle.extension().equals("prefab")) {
            SceneEditorWorkspace workspace = SceneEditorAddon.get().workspace;
            Vector2 sceneCords = workspace.getMouseCordsOnScene();
            Prefab prefab = Prefab.from(fileHandle);
            GameObject gameObject = workspace.createFromPrefab(prefab, sceneCords, parent);
        }
    }


    public static void findInPath(FileHandle path, String extensionFilter, Array<FileHandle> result) {
        if (path.isDirectory()) {
            FileHandle[] list = path.list();
            for(int i = 0; i < list.length; i++) {
                FileHandle item = list[i];
                findInPath(item, extensionFilter, result);
            }
        } else {
            if(path.extension().equals(extensionFilter)) {
                result.add(path);
            }
        }
    }

    public static String checkSum(FileHandle fileHandle) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            // DigestInputStream is better, but you also can hash file like this.
            try (InputStream fis = new FileInputStream(fileHandle.path())) {
                byte[] buffer = new byte[1024];
                int nread;
                while ((nread = fis.read(buffer)) != -1) {
                    md.update(buffer, 0, nread);
                }
            }

            // bytes to hex
            StringBuilder result = new StringBuilder();
            for (byte b : md.digest()) {
                result.append(String.format("%02x", b));
            }
            return result.toString();

        } catch (Exception e) {

        }

        return "";
    }

    public static void saveMetadata (AMetadata aMetadata) {
        FileHandle assetHandle = aMetadata.currentFile;
        if(assetHandle != null && assetHandle.exists()) {
            FileHandle metadataHandle = getMetadataHandleFor(assetHandle);
            saveMetadata(metadataHandle, aMetadata);

            SceneEditorWorkspace workspace = SceneEditorAddon.get().workspace;
            workspace.clearMetadata(AssetImporter.relative(assetHandle.path()));
        }
    }

    public static void saveMetadata (FileHandle handle, AMetadata aMetadata) {
        String data = json.toJson(aMetadata);
        handle.writeString(data, false);
    }

    public static FileHandle makeSimilar (FileHandle fileHandle, String extension) {
        String path = fileHandle.parent().path() + File.separator + fileHandle.nameWithoutExtension() + "." + extension;
        return Gdx.files.absolute(path);
    }

    public static FileHandle suggestNewName (String path, String filename, String extension) {
        FileHandle handle = Gdx.files.absolute(path);

        if(handle.exists() && handle.isDirectory()) {
            String name = filename + "." + extension;
            FileHandle newFile = Gdx.files.absolute(handle.path() + File.separator + name);
            int i = 0;
            while (newFile.exists()) {
                name = filename + " " + (i++) + "." + extension;
                newFile = Gdx.files.absolute(handle.path() + File.separator + name);
            }

            return newFile;
        } else {
            throw new GdxRuntimeException("Path not a directory, path: " + path);
        }
    }

    public static void fileOpen (FileHandle fileHandle) {
        if(fileHandle.isDirectory()) {
            SceneEditorAddon.get().projectExplorer.select(fileHandle.path());
            return;
        }
        if(fileHandle.extension().equals("scn")) {
            SceneEditorAddon.get().workspace.openScene(fileHandle);
            return;
        } else if(fileHandle.extension().equals("prefab")) {
            SceneEditorAddon.get().workspace.openPrefab(fileHandle);
            return;
        } else if(fileHandle.extension().equals("tls")) {
            TalosMain.Instance().ProjectController().setProject(ProjectController.TLS);
            TalosMain.Instance().ProjectController().loadProject(fileHandle);
            return;
        } else if(fileHandle.extension().equals("js") || fileHandle.extension().equals("json") || fileHandle.extension().equals("ts")) {
            if (Desktop.isDesktopSupported()) {
                FileOpener.open(fileHandle.file());
            }
            return;
        }

        if (Desktop.isDesktopSupported()) {
            FileOpener.open(fileHandle.file());
        }
    }

    public static FileHandle get(String path) {
        String projectPath = SceneEditorAddon.get().workspace.getProjectPath();
        if(path.startsWith(projectPath)) {
            return Gdx.files.absolute(path);
        }

        String fullPath = projectPath + File.separator + path;

        return Gdx.files.absolute(fullPath);
    }

    public static String relative(String fullPath) {
        return relative(Gdx.files.absolute(fullPath));
    }

    public static String relative(FileHandle fileHandle) {
        String projectPath = SceneEditorAddon.get().workspace.getProjectPath();

        String path = fileHandle.path();
        if(path.startsWith(projectPath)) {
            path = path.substring(projectPath.length());
        }

        return path;
    }

    public static void copyFile(FileHandle file, FileHandle directory) {
        //Exchange it for the AssetRepository
        AssetRepository.getInstance().copyRawAsset(file, directory);
    }

    public static void moveFile(FileHandle file, FileHandle directory) {
        String projectPath = SceneEditorAddon.get().workspace.getProjectPath();

        //Protected, don't move assets/scenes folders
        if(file.path().equals(projectPath + File.separator + "assets")) return;
        if(file.path().equals(projectPath + File.separator + "scenes")) return;

        AssetRepository.getInstance().moveFile(file, directory);
    }

    public static FileHandle renameFile(FileHandle file, String newName) {
        String projectPath = SceneEditorAddon.get().workspace.getProjectPath();
        if(file.path().equals(projectPath + File.separator + "assets")) return file;

        if(!file.isDirectory()) {
            String extension = file.extension();
            if (!newName.endsWith(extension)) {
                newName += "." + extension;
            }
        }

        FileHandle newHandle = file.parent().child(newName);

        if(file.path().equals(newHandle.path())) return file;

        AssetRepository.getInstance().moveFile(file, newHandle);

        return newHandle;
    }

    public static void deleteFile(FileHandle file) {
        AssetRepository.getInstance().deleteRawAsset(file);
    }
}