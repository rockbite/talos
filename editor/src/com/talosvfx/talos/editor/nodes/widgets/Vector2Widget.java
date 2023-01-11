package com.talosvfx.talos.editor.nodes.widgets;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.project2.SharedResources;


public class Vector2Widget extends AbstractWidget<Vector2> {

    private Vector2 val = new Vector2();
    private ValueWidget xWidget;
    private ValueWidget yWidget;

    @Override
    public void init(Skin skin) {
        super.init(skin);

        xWidget = new ValueWidget(SharedResources.skin);
        xWidget.setLabel("X: ");
        xWidget.setType(ValueWidget.Type.TOP);
        yWidget = new ValueWidget(SharedResources.skin);
        yWidget.setLabel("Y: ");
        yWidget.setType(ValueWidget.Type.BOTTOM);

        content.add(xWidget).growX();
        content.row();
        content.add(yWidget).growX();

        xWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                val.x = xWidget.getValue();
            }
        });

        yWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                val.y = yWidget.getValue();
            }
        });
    }

    @Override
    public void loadFromXML(XmlReader.Element element) {
            // todo: impl later
    }

    @Override
    public Vector2 getValue() {
        return val;
    }

    @Override
    public void read(Json json, JsonValue jsonValue) {
        float x = jsonValue.getFloat("x");
        float y = jsonValue.getFloat("y");
        val.set(x, y);

        xWidget.setValue(x);
        yWidget.setValue(y);
    }

    @Override
    public void write(Json json, String name) {
        json.writeObjectStart(name);
        json.writeValue("x", val.x);
        json.writeValue("y", val.y);
        json.writeObjectEnd();
    }
}
