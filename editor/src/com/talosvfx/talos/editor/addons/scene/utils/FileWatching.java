package com.talosvfx.talos.editor.addons.scene.utils;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.events.ScriptFileChangedEvent;
import com.talosvfx.talos.editor.notifications.Notifications;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class FileWatching {
    private Thread workingThread;

    private final Object lock = new Object();
    private WatchService watchService;

    private volatile boolean shutdown;

    private HashMap<WatchKey, Path> watchKeys = new HashMap<>();

    private Changes changes = new Changes();

    public static class Changes {
        public Array<FileHandle> added = new Array<>();
        public Array<FileHandle> removed = new Array<>();
        public Array<FileHandle> changed = new Array<>();

        void reset () {
            added.clear();
            removed.clear();
            changed.clear();
        }

        public boolean hasChanges () {
            return added.size > 0 || removed.size > 0 || changed.size > 0;
        }

        public boolean directoryStructureChange () {
            return added.size > 0 || removed.size > 0;
        }
    }

    public FileWatching () {
        workingThread = new Thread(this::run, "Watcher thread");
        workingThread.start();
    }

    public void startWatchingCurrentProject () throws IOException {
        synchronized (lock) {

            if (watchService != null) {
                watchService.close();
            }

            watchService = FileSystems.getDefault().newWatchService();

            Path project = Paths.get(SceneEditorAddon.get().workspace.getProjectPath());
            directoryModification(project, this::registerDirectory);
            directoryModification(AssetRepository.getExportedScriptsFolderHandle().file().toPath(), this::registerDirectory);
        }
    }

    private void directoryModification (Path path, Function<Path, FileVisitResult> function) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory (Path dir, BasicFileAttributes attrs) {
                    return function.apply(dir);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private FileVisitResult registerDirectory (Path dir) {

        try {
            WatchKey key = dir.register(watchService,
                    new WatchEvent.Kind[] {
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_DELETE,
                            StandardWatchEventKinds.ENTRY_MODIFY
                    },
                    new WatchEvent.Modifier[0]);
            watchKeys.put(key, dir);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return FileVisitResult.CONTINUE;
    }

    private FileVisitResult unRegisterDirectory (Path path) {

        WatchKey watchKeyForDir = getWatchKeyForDir(path);
        if (watchKeyForDir != null) {
            watchKeyForDir.cancel();
            watchKeys.remove(watchKeyForDir);
        }

        return FileVisitResult.CONTINUE;
    }

    private WatchKey getWatchKeyForDir (Path dir) {
        for (Map.Entry<WatchKey, Path> entry : watchKeys.entrySet()) {
            if (entry.getValue() == dir) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void shutdown () {
        shutdown = true;
    }

    private void run () {
        while (!shutdown) {
            try {

                synchronized (lock) {

                    changes.reset();

                    if (watchService != null) {
                        WatchKey watchKey;

                        int counter = 0;
                        int interval = 50;
                        while (counter < 1000) {
                            while ((watchKey = watchService.poll(interval, TimeUnit.MILLISECONDS)) != null) {
                                Path parent = watchKeys.get(watchKey);

                                watchKey.pollEvents().stream().filter(e -> e.kind() != StandardWatchEventKinds.OVERFLOW)
                                        .map(e -> ((WatchEvent<Path>)e)).forEach(e -> {
                                    Path p = e.context();

                                    final Path absolutePath = parent.resolve(p);

                                    onFileWatchEvent(e, parent, absolutePath);
                                });

                                watchKey.reset();
                            }
                            counter += interval;
                        }
                    }
                }

                if(changes.hasChanges()) {
                    //Notifications.fireEvent(Notifications.obtainEvent(ProjectDirectoryContentsChanged.class).set(changes));
                }

                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void onFileWatchEvent (WatchEvent<Path> event, Path keyPath, Path path) {
        File file = path.toFile();
        if (file.isDirectory()) {
            //We need to add a new watch, and also
            if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                directoryModification(keyPath, this::unRegisterDirectory);
            } else if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                directoryModification(keyPath, this::registerDirectory);
            }

            if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE || event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                try {
                    Files.walk(file.toPath()).forEach(p -> {
                        if (!p.toFile().isDirectory()) {
                            registerWatchActivities(event, p.toFile());
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            registerWatchActivities(event, file);
        }
    }

    private void registerWatchActivities (WatchEvent<Path> event, File file) {
        FileHandle handle = pathToFileHandle(file);
        String scriptFolderPath = AssetRepository.getExportedScriptsFolderHandle().path();

        if (handle.path().contains(scriptFolderPath)) {
            // a script file is changed
            if (!SceneEditorWorkspace.getInstance().exporting) {
                Notifications.fireEvent(Notifications.obtainEvent(ScriptFileChangedEvent.class).set(event.kind(), handle));
            }
        }

        if (changes != null) {
            if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                changes.removed.add(pathToFileHandle(file));
            } else if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                changes.added.add(pathToFileHandle(file));
            } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                changes.changed.add(pathToFileHandle(file));
            }
        }
    }

    private FileHandle pathToFileHandle (File file) {
        return new FileHandle(file);
    }

}
