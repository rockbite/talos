package com.talosvfx.talos.editor.nodes.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public abstract class AbstractWidget<T> extends Table {

    protected Table content;
    protected Table portContainer;

    public AbstractWidget() {
        content = new Table();
        portContainer = new Table();

        Stack mainStack = new Stack();

        mainStack.add(content);
        mainStack.add(portContainer);

        add(mainStack).grow();
    }

    public void init(Skin skin) {
        setSkin(skin);
    }

    public abstract void loadFromXML(XmlReader.Element element);

    public Table addPort(boolean isInput) {
        portContainer.clearChildren();

        Table portBody = new Table();

        Image portBorder = new Image(ColorLibrary.obtainBackground(getSkin(), "circle-border", ColorLibrary.BackgroundColor.BROKEN_WHITE));
        portBody.setBackground(ColorLibrary.obtainBackground(getSkin(), ColorLibrary.SHAPE_CIRCLE, ColorLibrary.BackgroundColor.BROKEN_WHITE));
        portBody.add(portBorder).growX().pad(-1f);

        float offsetPadding = 20;

        if (isInput) {
            portContainer.left();
            portContainer.add(portBody).size(15, 15).padLeft(-offsetPadding);
        } else {
            portContainer.right();
            portContainer.add(portBody).size(15, 15).padRight(-offsetPadding);
        }


        return portBody;
    }

    protected boolean fireChangedEvent() {
        ChangeListener.ChangeEvent changeEvent = Pools.obtain(ChangeListener.ChangeEvent.class);

        boolean var2;
        try {
            var2 = fire(changeEvent);
        } finally {
            Pools.free(changeEvent);
        }

        return var2;
    }

    public abstract T getValue();

    public abstract void read (Json json, JsonValue jsonValue);
    public abstract void write (Json json, String name);
}
