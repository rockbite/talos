package com.talosvfx.talos.editor.notifications;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pools;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.notifications.events.AssetFileDroppedEvent;
import com.talosvfx.talos.editor.project.FileTracker;

public class FileActorBinder implements Notifications.Observer {

    private static FileActorBinder instance;

    private Vector2 vec = new Vector2();
    private ObjectMap<Actor, String> extensionMap = new ObjectMap<>();

    private FileActorBinder () {
        Notifications.registerObserver(this);
    }

    private static FileActorBinder getInstance() {
        if(instance == null) {
            instance = new FileActorBinder();
        }

        return instance;
    }

    private void registerInner (Actor actor, String extension) {

        extensionMap.put(actor, extension);

        actor.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                TalosMain.Instance().UIStage().showFileChooser(extension, new FileChooserAdapter() {
                    @Override
                    public void selected (Array<FileHandle> file) {
                        String path = file.first().file().getAbsolutePath();
                        FileHandle handle = Gdx.files.absolute(path);

                        FileEvent fileEvent = Pools.obtain(FileEvent.class);
                        fileEvent.setFileHandle(handle);
                        actor.fire(fileEvent);
                    }
                });
            }
        });

    }

    public static void register (Actor actor, String extension) {
        FileActorBinder fileActorBinder = getInstance();
        fileActorBinder.registerInner(actor, extension);
    }

    static abstract public class FileEventListener implements EventListener {

        @Override
        public boolean handle (Event event) {
            if(event instanceof FileEvent) {
                FileHandle fileHandle = ((FileEvent) event).getFileHandle();
                onFileSet(fileHandle);

                TalosMain.Instance().FileTracker().trackFile(fileHandle, new FileTracker.Tracker() {
                    @Override
                    public void updated (FileHandle handle) {
                        onFileSet(fileHandle);
                    }
                });
            }

            return false;
        }

        public abstract void onFileSet(FileHandle fileHandle);
    }

    static public class FileEvent extends Event {

        private FileHandle fileHandle;

        public FileHandle getFileHandle () {
            return fileHandle;
        }

        public void setFileHandle (FileHandle fileHandle) {
            this.fileHandle = fileHandle;
        }
    }

    @EventHandler
    public void onAssetFileDroppedEvent (AssetFileDroppedEvent event) {
        FileHandle fileHandle = event.getFileHandle();

        for(Actor actor: extensionMap.keys()) {
            String extension = extensionMap.get(actor); // todo: well this structure can be changed, as filter will get more complicated

            if(fileHandle.extension().equals(extension) && actor.getStage() != null) {
                vec.set(event.getScreenPos());
                vec = actor.screenToLocalCoordinates(vec);
                if (actor.hit(vec.x, vec.y, false) != null) {
                    FileEvent fileEvent = Pools.obtain(FileEvent.class);
                    fileEvent.setFileHandle(fileHandle);
                    actor.fire(fileEvent);

                    break;
                }
            }
        }
    }
}
