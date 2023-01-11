package com.talosvfx.talos.editor.addons.scene.apps;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

public abstract class AEditorApp<T> {

    protected Table content;
    protected String identifier;
    protected T object;

    protected Array<AppListener> appListeners = new Array<>();

    public void onHide () {

    }

    public interface AppListener {
        void closeRequested();
    }

    public boolean notifyClose() {

        for (AppListener appListener : appListeners) {
            appListener.closeRequested();
        }

        return true;
    }

    public enum AppOpenStrategy {
        WINDOW,
        BOTTOM_TAB,
        RIGHT_TAB
    }

    public AEditorApp(T object) {
        this.object = object;
    }

    public abstract void initContent();

    public abstract String getTitle();

    public Table getContent() {
        return content;
    }

    public void addAppListener(AppListener listener) {
        this.appListeners.add(listener);
    }
}
