package com.talosvfx.talos.editor.dialogs.preference.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.XmlReader;

public abstract class PrefRowWidget extends APrefWidget {

    protected Table leftContent;
    protected Table rightContent;

    public PrefRowWidget(String parentPath, XmlReader.Element xml) {
        super(parentPath, xml);
        build();
    }

    private void build() {

        leftContent = new Table();
        rightContent = new Table();

        add(leftContent).width(200).padRight(5);
        add(rightContent).growX();
    }
}
