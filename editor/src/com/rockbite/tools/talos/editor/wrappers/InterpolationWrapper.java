package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.rockbite.tools.talos.runtime.modules.InterpolationModule;
import com.rockbite.tools.talos.runtime.utils.InterpolationMappings;

public class InterpolationWrapper extends ModuleWrapper<InterpolationModule> {


    VisSelectBox<String> selectBox;

    public InterpolationWrapper() {
        super();
    }

    @Override
    protected float reportPrefWidth() {
        return 250;
    }

    @Override
    protected void configureSlots() {

        addInputSlot("alpha (0 to 1)", InterpolationModule.ALPHA);

        addOutputSlot("output", 0);

        Array<String> interps = new Array<>();
        InterpolationMappings.getAvailableInterpolations(interps);

        selectBox = addSelectBox(interps);

        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedString = selectBox.getSelected();

                Interpolation interp = InterpolationMappings.getInterpolationForName(selectedString);

                module.setInterpolation(interp);
            }
        });
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        selectBox.setSelected(InterpolationMappings.getNameForInterpolation(module.getInterpolation()));
    }

}
