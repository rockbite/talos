package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.editor.widgets.IntegerInputWidget;
import com.rockbite.tools.talos.runtime.modules.PolylineModule;

public class PolylineModuleWrapper extends ModuleWrapper<PolylineModule> {

    private IntegerInputWidget interpolationPoints;

    @Override
    protected void configureSlots() {
        addInputSlot("offset",  PolylineModule.OFFSET);
        addInputSlot("thickness",  PolylineModule.THICKNESS);
        addInputSlot("color",  PolylineModule.COLOR);
        addInputSlot("transparency",  PolylineModule.TRANSPARENCY);


        interpolationPoints = new IntegerInputWidget("interpolation points", getSkin());
        interpolationPoints.setValue(0);
        leftWrapper.add(interpolationPoints).left().expandX();

        interpolationPoints.setListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                module.setInterpolationPoints(interpolationPoints.getValue());
            }
        });

        rightWrapper.add().growY().row();
        addOutputSlot("output", PolylineModule.OUTPUT);
    }

    @Override
    protected float reportPrefWidth() {
        return 180;
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        interpolationPoints.setValue(module.pointCount - 2);
    }
}
