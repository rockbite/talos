package com.talosvfx.talos.editor.addons.scene.apps;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.apps.spriteeditor.SpriteEditor;

public class SEAppManager {

    private ObjectMap<String, AEditorApp> openedApps = new ObjectMap<>();


    public void openApp(AEditorApp editorApp, AEditorApp.AppOpenStrategy strategy) {
        if(openedApps.containsKey(editorApp.identifier)) {
            for (Tab tab : SceneEditorAddon.get().bottomTabbedPane.getTabs()) {
                if(tab instanceof AppTab) {
                    AppTab appTab = (AppTab) tab;
                    if(appTab.app.identifier.equals(editorApp.identifier)) {
                        SceneEditorAddon.get().bottomTabbedPane.switchTab(appTab);
                        return;
                    }
                }
            }
            return;
        }

        if (strategy == AEditorApp.AppOpenStrategy.BOTTOM_TAB || strategy == AEditorApp.AppOpenStrategy.RIGHT_TAB) {
            AppTab newTab = new AppTab(editorApp) {
                @Override
                public String getTabTitle() {
                    return editorApp.getTitle();
                }

                @Override
                public Table getContentTable() {
                    return editorApp.getContent();
                }
            };
            editorApp.addAppListener(new AEditorApp.AppListener() {
                @Override
                public void closeRequested() {
                    newTab.removeFromTabPane();
                }
            });

            SceneEditorAddon.get().bottomTabbedPane.add(newTab);
        } else if(strategy == AEditorApp.AppOpenStrategy.WINDOW) {
            AppWindow window = new AppWindow(editorApp);

            editorApp.addAppListener(new AEditorApp.AppListener() {
                @Override
                public void closeRequested() {
                    window.hide();
                }
            });

            window.show();
        }

        openedApps.put(editorApp.identifier, editorApp);
    }

    public void notifyClosed(AEditorApp app) {
        openedApps.remove(app.identifier);
    }

    public void close(SpriteEditor app) {
        notifyClosed(app);
        app.notifyClose();
    }

    class AppWindow extends VisWindow {

        private AEditorApp app;
        public AppWindow(AEditorApp app) {
            super("");
            this.app = app;
            getTitleLabel().setText(app.getTitle());

            getStyle().stageBackground = null;

            setCenterOnAdd(true);
            setResizable(true);
            setMovable(true);
            addCloseButton();
            closeOnEscape();

            add(app.getContent()).grow();

            pack();
            invalidate();

            centerWindow();
        }

        public AEditorApp show() {
            TalosMain.Instance().UIStage().getStage().addActor(this);
            return app;
        }
        public void hide() {
            remove();
        }

        @Override
        public boolean remove() {
            SceneEditorAddon.get().seAppManager.notifyClosed(app);
            return super.remove();
        }
    }

    public abstract class AppTab extends Tab {

        AEditorApp app;

        public AppTab(AEditorApp app) {
            this.app = app;
        }

        public AEditorApp getApp() {
            return app;
        }
    }
}
