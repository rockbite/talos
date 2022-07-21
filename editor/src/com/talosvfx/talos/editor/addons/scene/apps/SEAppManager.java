package com.talosvfx.talos.editor.addons.scene.apps;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;

public class SEAppManager {

    private ObjectMap<String, AEditorApp> openedApps = new ObjectMap<>();


    public void openApp(AEditorApp editorApp, AEditorApp.AppOpenStrategy strategy) {
        if(openedApps.containsKey(editorApp.identifier)) {
            return;
        }

        if (strategy == AEditorApp.AppOpenStrategy.BOTTOM_TAB || strategy == AEditorApp.AppOpenStrategy.RIGHT_TAB) {
            Tab newTab = new Tab() {
                @Override
                public String getTabTitle() {
                    return editorApp.getTitle();
                }

                @Override
                public Table getContentTable() {
                    return editorApp.getContent();
                }
            };

            SceneEditorAddon.get().bottomTabbedPane.add(newTab);
        } else if(strategy == AEditorApp.AppOpenStrategy.WINDOW) {
            AppWindow window = new AppWindow(editorApp);
            window.show();
        }

        openedApps.put(editorApp.identifier, editorApp);
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

            add(app.getContent());

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
    }
}
