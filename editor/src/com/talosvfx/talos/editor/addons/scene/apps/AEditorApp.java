package com.talosvfx.talos.editor.addons.scene.apps;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.talosvfx.talos.TalosMain;

public abstract class AEditorApp<T> {

    protected Table content;
    protected String identifier;
    protected T object;

    public AEditorApp(T object) {
        this.object = object;
        /*
        super("");
        getTitleLabel().setText(getTitle());

        title = getTitle();

        getStyle().stageBackground = null;

        setCenterOnAdd(true);
        setResizable(true);
        setMovable(true);
        addCloseButton();
        closeOnEscape();

        initContent();

        pack();
        invalidate();

        centerWindow();*/
    }

    protected abstract void initContent();

    protected abstract String getTitle();

    public Table getContent() {
        return content;
    }

/*
    public AEditorApp show() {
        TalosMain.Instance().UIStage().getStage().addActor(this);
        return this;
    }
    public void hide() {
        remove();
    }*/
}
