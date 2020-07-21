package com.talosvfx.talos.editor.notifications.events;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.editor.notifications.Notifications;

public class AssetFileDroppedEvent implements Notifications.Event {

    private Vector2 screenPos = new Vector2();
    private FileHandle fileHandle;

    @Override
    public void reset () {
        fileHandle = null;
        screenPos.set(0,0);
    }

    public Vector2 getScreenPos () {
        return screenPos;
    }

    public void setScreenPos (float x, float y) {
        this.screenPos.set(x, y);
    }

    public FileHandle getFileHandle () {
        return fileHandle;
    }

    public void setFileHandle (FileHandle fileHandle) {
        this.fileHandle = fileHandle;
    }
}
