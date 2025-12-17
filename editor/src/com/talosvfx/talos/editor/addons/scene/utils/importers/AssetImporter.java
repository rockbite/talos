package com.talosvfx.talos.editor.addons.scene.utils.importers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.CharArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.utils.Toasts;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.events.FileNameChanged;
import com.talosvfx.talos.runtime.scene.GameObject;import com.talosvfx.talos.runtime.assets.AMetadata;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.assets.AssetChangeDirectoryEvent;
import com.talosvfx.talos.editor.notifications.events.assets.GameAssetOpenEvent;
import com.talosvfx.talos.editor.project.FileTracker;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.runtime.utils.NamingUtils;
import com.talosvfx.talos.runtime.utils.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;

public class AssetImporter {

    private static final Logger logger = LoggerFactory.getLogger(AssetImporter.class);
    public static boolean fromDirectoryView;
    public FileTracker.Tracker assetTracker;

    private static ObjectMap<GameAssetType, AbstractImporter> importerMap = new ObjectMap<>();

    private static Json json = new Json();
    static {
        json.setOutputType(JsonWriter.OutputType.json);
    }

    public AssetImporter() {
        assetTracker = handle -> assetUpdated(handle);

        importerMap.put(GameAssetType.SPRITE, new SpriteImporter());
        importerMap.put(GameAssetType.VFX, new TlsImporter());
        importerMap.put(GameAssetType.SKELETON, new SpineImporter());
        importerMap.put(GameAssetType.ATLAS, new AtlasImporter());
        importerMap.put(GameAssetType.PREFAB, new PrefabImporter());

    }

    public static FileHandle attemptToImport (FileHandle handle) {
        return attemptToImport(handle, false);
    }

    public static FileHandle attemptToImport (FileHandle handle, boolean andPlaceIt) {
        logger.info("Disabled method");

//        FileHandle destinationDir = SceneEditorAddon.get().projectExplorer.getCurrentFolder();
//
//        AssetRepository.getInstance().copyRawAsset(handle, destinationDir);
//
//        if (andPlaceIt) {
//            System.out.println("Placing needs implementing again");
//        }
//
//        String projectPath = SceneEditorAddon.get().workspace.getProjectPath();
//        SceneEditorAddon.get().projectExplorer.loadDirectoryTree(projectPath);
//        SceneEditorAddon.get().projectExplorer.expand(destinationDir.path());
//        SceneEditorAddon.get().projectExplorer.select(destinationDir.path());
//
//        TalosMain.Instance().ProjectController().saveProject();

        return handle;
    }

    private void assetUpdated (FileHandle handle) {

        //todo update asset event
        logger.info("Need to update asset with event");
//        SceneEditorAddon.get().workspace.updateAsset(handle); //todo: maybe instead worth using events
    }

    // Check up on things, tidy up a bit
//    public void housekeep (String projectPath) {
//        FileHandle project = Gdx.files.absolute(projectPath);
//        Array<FileHandle> tlsFiles = new Array<>();
//        findInPath(project, "tls", tlsFiles);
//
//        for(FileHandle tlsHandle: tlsFiles) {
//            String checksum = checkSum(tlsHandle);
//            TlsMetadata tlsMetadata = readMetadataFor(tlsHandle, TlsMetadata.class);
//            if(!tlsMetadata.tlsChecksum.equals(checksum)) {
//                ((TlsImporter)importerMap.get(AssetType.TLS)).exportTlsFile(tlsHandle);
//
//                tlsMetadata.tlsChecksum = checksum;
//                FileHandle metadataHandle = AssetImporter.getMetadataHandleFor(tlsHandle);
//                AssetImporter.saveMetadata(metadataHandle, tlsMetadata);
//            }
//        }
//    }


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
//        handle = get(handle.path());

        FileHandle metadataHandle = Gdx.files.absolute(handle.parent().path() + File.separator + handle.name() + ".meta");
        return metadataHandle;
    }

    public static GameObject createAssetInstance(GameAsset<?> gameAsset, GameObject parent) {
        AbstractImporter abstractImporter = importerMap.get(gameAsset.type);

        if (abstractImporter != null) {
            GameObject gameObject = abstractImporter.makeInstance(gameAsset, parent);
            return gameObject;
        } else {
            System.out.println("No importer found for type " + gameAsset.type);
            return null;
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
            CharArray result = new CharArray();
            for (byte b : md.digest()) {
                result.append(String.format("%02x", b));
            }
            return result.toString();

        } catch (Exception e) {

        }

        return "";
    }

    public static void saveMetadata (AMetadata aMetadata) {

        FileHandle assetHandle = aMetadata.link.handle;
        if(assetHandle != null && assetHandle.exists()) {
            FileHandle metadataHandle = getMetadataHandleFor(assetHandle);
            saveMetadata(metadataHandle, aMetadata);
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

    public static FileHandle suggestNewNameForFileHandle (String path, String filename, String extension) {
        FileHandle handle = Gdx.files.absolute(path);


        if(handle.exists() && handle.isDirectory()) {
            filename = NamingUtils.getNewName(filename, new Supplier<Collection<String>>() {
                @Override
                public Collection<String> get () {
                    ArrayList<String> fileNames = new ArrayList<>();
                    for (FileHandle fileHandle : handle.list()) {
                        fileNames.add(fileHandle.nameWithoutExtension());
                    }
                    return fileNames;
                }
            });

            String name = filename + "." + extension;
            FileHandle newFile = Gdx.files.absolute(handle.path() + File.separator + name);

            return newFile;
        } else {
            throw new GdxRuntimeException("Path not a directory, path: " + path);
        }
    }

    public static void fileOpen (FileHandle fileHandle) {
        if(fileHandle.isDirectory()) {
            AssetChangeDirectoryEvent assetChangeDirectoryEvent = Notifications.obtainEvent(AssetChangeDirectoryEvent.class);
            assetChangeDirectoryEvent.setPath(fileHandle);
            Notifications.fireEvent(assetChangeDirectoryEvent);
            return;

        }


        //Game resource event
        GameAsset<?> assetForPath = AssetRepository.getInstance().getAssetForPath(fileHandle, true);
        if (assetForPath != null) {
            if (assetForPath.isBroken()) {
                Toasts.getInstance().showErrorToast("Asset is broken, you cannot open it");
                return;
            }
            if (SharedResources.appManager.canOpenInTalos(assetForPath)) {
                SharedResources.appManager.openNewAsset(assetForPath);
                return;
            }

            GameAssetOpenEvent resourceOpenEvent = Notifications.obtainEvent(GameAssetOpenEvent.class);
            resourceOpenEvent.setGameAsset(assetForPath);
            Notifications.fireEvent(resourceOpenEvent);
            return;
        }
        logger.info("todo - loading of open file needs to be done via evensts only");
//
//        if(fileHandle.extension().equals("scn")) {
//            //todo
//            SceneEditorAddon.get().workspace.openScene(fileHandle);
//            return;
//        } else if(fileHandle.extension().equals("prefab")) {
//            SceneEditorAddon.get().workspace.openPrefab(fileHandle);
//            return;
//        } else if(fileHandle.extension().equals("tls")) {
//            TalosMain.Instance().ProjectController().setProject(ProjectController.TLS);
//            TalosMain.Instance().ProjectController().loadProject(fileHandle);
//            return;
//        } else if (fileHandle.extension().equals("js") || fileHandle.extension().equals("ts")) {
//            FileHandle exportedScriptsFolderHandle = AssetRepository.getExportedScriptsFolderHandle();
//            FileHandle targetScriptInSrc = exportedScriptsFolderHandle.child(fileHandle.name());
//            if (targetScriptInSrc.exists()) {
//                FileOpener.open(targetScriptInSrc.file());
//            } else {
//                FileOpener.open(fileHandle.file());
//            }
//            return;
//        } else if (fileHandle.extension().equals("json")) {
//            FileOpener.open(fileHandle.file());
//            return;
//        } else if(fileHandle.extension().equals("ttp")) {
//            GameAsset<TilePaletteData> paletteData = (GameAsset<TilePaletteData>) AssetRepository.getInstance().getAssetForPath(fileHandle, true);
//            if (paletteData != null) {
//                SceneEditorAddon.get().openApp(new PaletteEditor(paletteData), AEditorApp.AppOpenStrategy.RIGHT_TAB);
//            } else {
//                System.out.println("Palette Asset not located");
//            }
//        } if(fileHandle.extension().equals("rw")) {
//            RoutineEditor editorApp = new RoutineEditor(fileHandle);
//            SceneEditorAddon.get().openApp(editorApp, AEditorApp.AppOpenStrategy.BOTTOM_TAB);
//            SceneEditorAddon.get().routineEditor = editorApp;
//            return;
//        } else {
//            FileOpener.open(fileHandle.file());
//        }
    }
    public static void copyFile(FileHandle file, FileHandle directory) {
        //Exchange it for the AssetRepository
        AssetRepository.getInstance().copyRawAsset(file, directory);
    }

    public static void moveFile(FileHandle file, FileHandle directory, boolean rename) {
        moveFile(file, directory, true, rename);
    }

    public static void moveFile(FileHandle file, FileHandle directory, boolean checkGameAssets, boolean rename) {
        //Are we moving the actual scene editing?

        logger.info("Redo scene editing move");
//        String path = SceneEditorWorkspace.getInstance().getCurrentContainer().path;
//        if (file.path().equals(path)) {
//            //moving the scene that we are editing
//            if (directory.isDirectory()) {
//                SceneEditorWorkspace.getInstance().getCurrentContainer().path = directory.child(file.name()).path();
//            } else {
//                SceneEditorWorkspace.getInstance().getCurrentContainer().path = directory.path();
//            }
//
//            SceneEditorWorkspace.getInstance().getCurrentContainer().setName(file.name());
//        }

        AssetRepository.getInstance().moveFile(file, directory, checkGameAssets, rename);
    }

    public static FileHandle renameFile(FileHandle file, String newName) {
        if(!file.isDirectory()) {
            String extension = file.extension();
            if (!newName.endsWith(extension)) {
                newName += "." + extension;
            }
        }

        FileHandle newHandle = file.parent().child(newName);

        if(file.path().equals(newHandle.path())) return file;

        AssetRepository.getInstance().moveFile(file, newHandle, true);

        if(!newHandle.isDirectory()) {
            FileNameChanged event = Notifications.obtainEvent(FileNameChanged.class);
            try {
                event.assetType = GameAssetType.getAssetTypeFromExtension(file.extension());
                event.oldName = file.nameWithoutExtension();
                event.newName = newHandle.nameWithoutExtension();
                Notifications.fireEvent(event);
            } catch (GameAssetType.NoAssetTypeException e) {
                throw new RuntimeException(e);
            }
        }

        return newHandle;
    }

    public static void deleteFile(FileHandle file) {
        AssetRepository.getInstance().deleteRawAsset(file);
    }
}
