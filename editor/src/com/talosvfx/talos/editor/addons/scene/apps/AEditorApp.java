package com.talosvfx.talos.editor.addons.scene.apps;

import com.kotcrab.vis.ui.widget.VisWindow;
import com.talosvfx.talos.TalosMain;

public abstract class AEditorApp extends VisWindow {

    public AEditorApp() {
        super("");
        getTitleLabel().setText(getTitle());

        getStyle().stageBackground = null;

        setCenterOnAdd(true);
        setResizable(true);
        setMovable(true);
        addCloseButton();
        closeOnEscape();

        initContent();

        pack();
        invalidate();

        centerWindow();
    }

    protected abstract void initContent();

    protected abstract String getTitle();

    public AEditorApp show() {
        TalosMain.Instance().UIStage().getStage().addActor(this);
        return this;
    }

    public void hide() {
        remove();
    }
}
