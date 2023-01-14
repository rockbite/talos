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

    private boolean isInput;
    private Table portBody;
    protected Image portBorder;

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

        this.isInput = isInput;

        portBody = new Table();
        portBorder = new Image(ColorLibrary.obtainBackground(getSkin(), "circle-border", ColorLibrary.BackgroundColor.BROKEN_WHITE));
        portBody.setBackground(ColorLibrary.obtainBackground(getSkin(), ColorLibrary.SHAPE_CIRCLE, ColorLibrary.BackgroundColor.BROKEN_WHITE));
        portBody.add(portBorder).growX().pad(-1f);

        portBody.setSize(15, 15);
        portContainer.addActor(portBody);


        if(isInput) {
            portBody.setX(-24);
        } else {
            portBody.setX(getWidth() + 9);
        }

        portBody.setY(getHeight()/2f - portBody.getHeight()/2f);

        return portBody;
    }

    @Override
    public void act(float delta) {
        if(portBody != null) {
            if (isInput) {
                portBody.setX(-24);
            } else {
                portBody.setX(getWidth() + 9);
            }
            portBody.setY(getHeight() / 2f - portBody.getHeight() / 2f);
        }
    }

    protected boolean fireChangedEvent() {
        ChangeListener.ChangeEvent changeEvent = Pools.obtain(ChangeListener.ChangeEvent.class);

        boolean var2 = false;
        try {
            var2 = fire(changeEvent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Pools.free(changeEvent);
        }

        return var2;
    }

    public boolean isFastChange() {
        return false;
    }

    public abstract T getValue();

    public boolean isChanged (T newValue) {
        T value = getValue();
        if (value == null) {
            return newValue == null;
        }
        return !value.equals(newValue);
    };

    public abstract void read (Json json, JsonValue jsonValue);
    public abstract void write (Json json, String name);
}
