package com.talosvfx.talos.editor.wrappers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.widgets.ShapeInputWidget;
import com.talosvfx.talos.runtime.modules.InterpolationModule;
import com.talosvfx.talos.runtime.modules.OffsetModule;
import com.talosvfx.talos.runtime.modules.ShapeModule;

public class ShapeModuleWrapper extends ModuleWrapper<ShapeModule> {

    private ShapeInputWidget shape;

    private Vector2 pos = new Vector2();
    private Vector2 size = new Vector2();

    private boolean lockUpdate = false;

    @Override
    protected void configureSlots() {

        addInputSlot("angle (0 to 1)", InterpolationModule.ALPHA);
        shape = new ShapeInputWidget(getSkin());

        contentWrapper.add(shape);


        addOutputSlot("output", OffsetModule.OUTPUT);

        if(module != null) {
            updateModuleDataFromWidgets();
        }

        shape.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                shape.getShapePos(pos);
                shape.getShapeSize(size);
                updateModuleDataFromWidgets();
            }
        });
    }

    private void updateModuleDataFromWidgets() {
        if(!lockUpdate) {
            shape.getShapePos(pos);
            module.setPos(pos);
            shape.getShapeSize(size);
            module.setSize(size);
            module.setShape(shape.getShape());
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        lockUpdate = true;
        super.read(json, jsonData);

        shape.setScaleVal(jsonData.getFloat("scale", 0));
        shape.setShape(jsonData.getInt("shape", 0));
        updateWidgetsFromModuleData();
        lockUpdate = false;
    }

    @Override
    public void write(Json json) {
        super.write(json);

        json.writeValue("scale", shape.getScale());
    }

    public void updateWidgetsFromModuleData() {
        lockUpdate = true;
        module.getPos(pos);
        shape.setPos(pos);
        module.getSize(size);
        shape.setShapeSize(size);

        shape.setShape(module.getShape());
        lockUpdate = false;
    }
}
